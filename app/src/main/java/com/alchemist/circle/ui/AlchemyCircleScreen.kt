package com.alchemist.circle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
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
    val dragThreshold = 55f

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
            HeaderSection(state = state)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularBoard(
                    slots = state.slots,
                    rotationAngle = state.rotationAngleDegrees,
                    isHammerActive = state.isHammerActive,
                    onSlotClick = { viewModel.onSlotTapped(it) },
                    modifier = Modifier.size(340.dp)
                )
            }

            ControlsSection(
                state = state,
                onHammerClick = { viewModel.toggleHammerMode() },
                onRotateLeft = { viewModel.makeMove(MoveDirection.COUNTER_CLOCKWISE) },
                onRotateRight = { viewModel.makeMove(MoveDirection.CLOCKWISE) }
            )
        }

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
        Spacer(modifier = Modifier.height(12.dp))
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
    onHammerClick: () -> Unit,
    onRotateLeft: () -> Unit,
    onRotateRight: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val hammerReady = state.hammerCharge >= 1.0f
        OutlinedButton(
            onClick = onHammerClick,
            enabled = hammerReady,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (state.isHammerActive) Color(0xFFFF1744) else if (hammerReady) Color(0xFF7C4DFF) else Color.Transparent
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Text(text = "🔨", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (state.isHammerActive) "Bir Element Seçin!" else if (hammerReady) "Atom Çekici Hazır!" else "Atom Çekici (%${(state.hammerCharge * 100).toInt()})",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        LinearProgressIndicator(
            progress = { state.hammerCharge },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(vertical = 6.dp)
                .height(6.dp),
            color = Color(0xFFFFD54F),
            trackColor = Color(0xFF2D2548),
        )

        Spacer(modifier = Modifier.height(10.dp))

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
