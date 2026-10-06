package com.alchemist.circle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alchemist.circle.model.GameState
import com.alchemist.circle.model.MoveDirection
import com.alchemist.circle.ui.components.CircularBoard
import com.alchemist.circle.viewmodel.AlchemyCircleViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun AlchemyCircleScreen(viewModel: AlchemyCircleViewModel) {
    val state by viewModel.uiState.collectAsState()

    var dragAmountX by remember { mutableStateOf(0f) }
    val dragThreshold = 40f

    // GÜNCELLEME KONTROLÜ
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var updateInfo by remember { mutableStateOf<com.alchemist.circle.data.UpdateManager.UpdateInfo?>(null) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val update = com.alchemist.circle.data.UpdateManager.checkForUpdates()
        if (update != null) {
            updateInfo = update
            showUpdateDialog = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF140D2E), // Koyu 2048 Gece Teması
                        Color(0xFF21103E),
                        Color(0xFF0F081E)
                    )
                )
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { dragAmountX = 0f },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragAmountX += dragAmount.x
                    },
                    onDragEnd = {
                        if (abs(dragAmountX) > dragThreshold) {
                            if (dragAmountX > 0) {
                                viewModel.makeMove(MoveDirection.CLOCKWISE)
                            } else {
                                viewModel.makeMove(MoveDirection.COUNTER_CLOCKWISE)
                            }
                        }
                    }
                )
            }
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. ÜST PANEL: 2048 Çember Başlığı ve Skor Kartları
            HeaderSection(state = state)

            // 2. ORTA ALAN: Ferah 8 Yuvalı 2048 Çemberi + Güç Kazanı
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularBoard(
                    slots = state.slots,
                    cauldronCharge = state.cauldronCharge,
                    lightningTargets = state.lightningTargets,
                    modifier = Modifier.size(370.dp)
                )
            }

            // 3. ALT ALAN: Güç Durumu ve 2 Büyük Çevirme Butonu
            ControlsSection(
                state = state,
                onRotateLeft = { viewModel.makeMove(MoveDirection.COUNTER_CLOCKWISE) },
                onRotateRight = { viewModel.makeMove(MoveDirection.CLOCKWISE) }
            )
        }

        // GÜNCELLEME DİYALOĞU
        if (showUpdateDialog && updateInfo != null) {
            val info = updateInfo!!
            AlertDialog(
                onDismissRequest = { if (!isDownloading) showUpdateDialog = false },
                containerColor = Color(0xFF28114B),
                title = {
                    Text("🚀 Yeni Güncelleme: ${info.newVersionTag}", color = Color(0xFFFFEB3B), fontSize = 20.sp, fontWeight = FontWeight.Black)
                },
                text = {
                    Column {
                        Text(
                            text = info.releaseNotes,
                            color = Color(0xFFE1BEE7),
                            fontSize = 14.sp
                        )
                        if (isDownloading) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "İndiriliyor: %${(downloadProgress * 100).toInt()}",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = Color(0xFF00E676),
                                trackColor = Color(0xFF38235F)
                            )
                        }
                    }
                },
                confirmButton = {
                    if (!isDownloading) {
                        Button(
                            onClick = {
                                isDownloading = true
                                coroutineScope.launch {
                                    val apk = com.alchemist.circle.data.UpdateManager.downloadApk(
                                        context = context,
                                        downloadUrl = info.downloadUrl,
                                        onProgress = { progress ->
                                            downloadProgress = progress
                                        }
                                    )
                                    isDownloading = false
                                    if (apk != null) {
                                        com.alchemist.circle.data.UpdateManager.installApk(context, apk)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("⚡ Şimdi Güncelle", color = Color(0xFF003300), fontWeight = FontWeight.Black)
                        }
                    }
                },
                dismissButton = {
                    if (!isDownloading) {
                        TextButton(onClick = { showUpdateDialog = false }) {
                            Text("Daha Sonra", color = Color(0xFFB39DDB))
                        }
                    }
                }
            )
        }

        // GAME OVER DİYALOĞU
        if (state.isGameOver) {
            GameOverDialog(
                score = state.score,
                bestScore = state.bestScore,
                onRestart = { viewModel.startNewGame() }
            )
        }
    }
}

@Composable
private fun HeaderSection(state: GameState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "✨ 2048 ÇEMBERİ ✨",
            color = Color(0xFFFFEB3B),
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ScoreBadge(title = "PUAN 🌟", score = state.score, color = Color(0xFFFF4081))
            ScoreBadge(title = "EN İYİ 🏆", score = state.bestScore, color = Color(0xFFFFD600))
        }
    }
}

@Composable
private fun ScoreBadge(title: String, score: Int, color: Color) {
    Surface(
        color = Color(0xFF2E1C53),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 6.dp,
        modifier = Modifier.width(135.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = "$score", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun ControlsSection(
    state: GameState,
    onRotateLeft: () -> Unit,
    onRotateRight: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val isReady = state.cauldronCharge >= 1.0f
        Text(
            text = if (isReady) "⚡ 2048 GÜÇ PATLAMASI: 2 TAŞ TEMİZLENDİ! 💥" else "🧪 2048 Güç Şarjı: %${(state.cauldronCharge * 100).toInt()}",
            color = if (isReady) Color(0xFFFFEB3B) else Color(0xFFFFD54F),
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { state.cauldronCharge },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(8.dp)
                .shadow(4.dp, RoundedCornerShape(4.dp)),
            color = if (isReady) Color(0xFFFFEB3B) else Color(0xFF00E676),
            trackColor = Color(0xFF38235F),
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2 Büyük, Canlı ve Oynaması Çok Kolay Çevirme Butonu
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(
                onClick = onRotateLeft,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .width(150.dp)
                    .height(52.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp))
            ) {
                Text(text = "🌀", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sola Çevir", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onRotateRight,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .width(150.dp)
                    .height(52.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp))
            ) {
                Text("Sağa Çevir", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "🌀", fontSize = 20.sp)
            }
        }
    }
}

@Composable
private fun GameOverDialog(score: Int, bestScore: Int, onRestart: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        containerColor = Color(0xFF28114B),
        title = {
            Text("🎉 Oyun Bitti!", color = Color(0xFFFF5252), fontSize = 22.sp, fontWeight = FontWeight.Black)
        },
        text = {
            Column {
                Text("Hamle yapacak yer kalmadı, harika oynadın!", color = Color(0xFFE1BEE7), fontSize = 15.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text("Toplam Puan: $score ⭐", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("En Yüksek Puan: $bestScore 🏆", color = Color(0xFFFFD600), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = {
            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("🔄 Tekrar Oyna", color = Color(0xFF003300), fontWeight = FontWeight.Black)
            }
        }
    )
}
