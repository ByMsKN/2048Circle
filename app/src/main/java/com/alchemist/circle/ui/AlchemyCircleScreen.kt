package com.alchemist.circle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val dragThreshold = 50f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F0C20), Color(0xFF1B1035), Color(0xFF090614))
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
            // 1. ÜST PANEL: Başlık ve Skorlar
            HeaderSection(state = state)

            // 2. ORTA ALAN: 10 Daireli Simya Yörüngesi + Merkezdeki Güç Kazanı
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
                    modifier = Modifier.size(360.dp)
                )
            }

            // 3. ALT ALAN: Güç Durumu ve Çevirme Kontrolleri
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
            text = "SİMYA ÇEMBERİ",
            color = Color(0xFFFFD54F),
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 3.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ScoreBadge(title = "SKOR", score = state.score)
            ScoreBadge(title = "EN İYİ", score = state.bestScore)
        }
    }
}

@Composable
private fun ScoreBadge(title: String, score: Int) {
    Surface(
        color = Color(0xFF251F3D),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.width(130.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, color = Color(0xFFB39DDB), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = "$score", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
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
        // Güç Kazanı İlerleme Çubuğu
        val isReady = state.cauldronCharge >= 1.0f
        Text(
            text = if (isReady) "⚡ YILDIRIM PATLAMASI TETİKLENDİ!" else "🔮 Güç Kazanı: %${(state.cauldronCharge * 100).toInt()}",
            color = if (isReady) Color(0xFF00E5FF) else Color(0xFFFFD54F),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { state.cauldronCharge },
            modifier = Modifier
                .fillMaxWidth(0.80f)
                .height(6.dp),
            color = if (isReady) Color(0xFF00E5FF) else Color(0xFFFFD54F),
            trackColor = Color(0xFF2D2548),
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Döndürme Butonları
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(
                onClick = onRotateLeft,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF312652)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = "◀", color = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sola Döndür", color = Color.White)
            }

            Button(
                onClick = onRotateRight,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF312652)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Sağa Döndür", color = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "▶", color = Color.White)
            }
        }
    }
}

@Composable
private fun GameOverDialog(score: Int, bestScore: Int, onRestart: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        containerColor = Color(0xFF1F1A33),
        title = {
            Text("Simya Döngüsü Doldu!", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text("Yapılabilecek hamle kalmadı.", color = Color(0xFFD1C4E9))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Kazanılan Skor: $score", color = Color.White, fontSize = 16.sp)
                Text("En Yüksek Skor: $bestScore", color = Color(0xFFFFD54F), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = {
            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EA))
            ) {
                Text("🔄 Yeniden Başlat")
            }
        }
    )
}
