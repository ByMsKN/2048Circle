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
import kotlin.math.abs

@Composable
fun AlchemyCircleScreen(viewModel: AlchemyCircleViewModel) {
    val state by viewModel.uiState.collectAsState()

    var totalDragX by remember { mutableStateOf(0f) }
    val dragThreshold = 45f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1A103C), // Canlı Gece Moru
                        Color(0xFF28114B),
                        Color(0xFF130924)
                    )
                )
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { totalDragX = 0f },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDragX += dragAmount.x
                    },
                    onDragEnd = {
                        if (abs(totalDragX) > dragThreshold) {
                            if (totalDragX > 0) {
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
            // 1. ÜST PANEL: Renkli Başlık ve Skor Kartları
            HeaderSection(state = state)

            // 2. ORTA ALAN: 10 Daireli Renkli Simya Çemberi + Güç Kazanı
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularBoard(
                    slots = state.slots,
                    rotationAngle = state.rotationAngleDegrees,
                    cauldronCharge = state.cauldronCharge,
                    lightningTargets = state.lightningTargets,
                    modifier = Modifier.size(370.dp)
                )
            }

            // 3. ALT ALAN: Güç Durumu ve Renkli Butonlar
            ControlsSection(
                state = state,
                onRotateLeft = { viewModel.makeMove(MoveDirection.COUNTER_CLOCKWISE) },
                onRotateRight = { viewModel.makeMove(MoveDirection.CLOCKWISE) }
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
            text = "✨ SİMYA ÇEMBERİ ✨",
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
            text = if (isReady) "⚡ YILDIRIM PATLAMASI ALAN AÇIYOR! 💥" else "🧪 Sihirli Güç Kazanı: %${(state.cauldronCharge * 100).toInt()}",
            color = if (isReady) Color(0xFFFFEB3B) else Color(0xFFFF80AB),
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
            color = if (isReady) Color(0xFFFFEB3B) else Color(0xFFFF4081),
            trackColor = Color(0xFF38235F),
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Canlı, Çocuksu ve Büyük Çevirme Butonları
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(
                onClick = onRotateLeft,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .width(145.dp)
                    .height(50.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp))
            ) {
                Text(text = "🌀", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sola Çevir", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onRotateRight,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .width(145.dp)
                    .height(50.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp))
            ) {
                Text("Sağa Çevir", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "🌀", fontSize = 18.sp)
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
