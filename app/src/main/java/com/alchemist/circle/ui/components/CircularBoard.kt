package com.alchemist.circle.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alchemist.circle.model.ElementTier
import com.alchemist.circle.model.GameState
import kotlin.math.*

@Composable
fun CircularBoard(
    slots: List<ElementTier?>,
    cauldronCharge: Float,
    lightningTargets: List<Int>,
    modifier: Modifier = Modifier
) {
    // Güç kazanı için nabız efekti
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Arka plan ışıltısı
    val magicGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "magicGlow"
    )

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val sizePx = constraints.maxWidth.coerceAtMost(constraints.maxHeight).toFloat()

        // 3 EŞMERKEZLİ HALKA GEOMETRİSİ:
        // İç Halka (r=0):  sizePx * 0.23f (~40dp slot)
        // Orta Halka (r=1): sizePx * 0.33f (~42dp slot)
        // Dış Halka (r=2):  sizePx * 0.43f (~44dp slot)
        val ringRadiiPx = listOf(
            sizePx * 0.22f, // İç Halka (Inner)
            sizePx * 0.32f, // Orta Halka (Middle)
            sizePx * 0.42f  // Dış Halka (Outer)
        )
        val slotSizes = listOf(36.dp, 40.dp, 44.dp)

        // 1. NEŞELİ 2048 ÇEMBER RAYLARI & ELEKTRİK EFEKTLERİ CANVAS
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)

            // Dış atmosferik sihir halesi
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF7C4DFF).copy(alpha = magicGlow * 0.25f),
                        Color(0xFFFF4081).copy(alpha = magicGlow * 0.10f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = ringRadiiPx[2] * 1.25f
                ),
                radius = ringRadiiPx[2] * 1.25f,
                center = center
            )

            // 3 Eşmerkezli Yörünge Halkası Çizimi
            ringRadiiPx.forEachIndexed { idx, radius ->
                drawCircle(
                    color = when (idx) {
                        0 -> Color(0xFF00E5FF).copy(alpha = 0.35f)
                        1 -> Color(0xFFFFD54F).copy(alpha = 0.35f)
                        else -> Color(0xFFFF4081).copy(alpha = 0.35f)
                    },
                    radius = radius,
                    center = center,
                    style = Stroke(width = (2.5f + idx).dp.toPx())
                )
            }

            // 8 Radyal Kılavuz Çizgi (İçten dışa sektör eksenleri)
            for (s in 0 until GameState.SLOTS_PER_RING) {
                val angleRad = (2.0 * PI / GameState.SLOTS_PER_RING) * s - (PI / 2.0)
                val startX = (ringRadiiPx[0] * 0.7f * cos(angleRad)).toFloat() + center.x
                val startY = (ringRadiiPx[0] * 0.7f * sin(angleRad)).toFloat() + center.y
                val endX = (ringRadiiPx[2] * 1.08f * cos(angleRad)).toFloat() + center.x
                val endY = (ringRadiiPx[2] * 1.08f * sin(angleRad)).toFloat() + center.y

                drawLine(
                    color = Color.White.copy(alpha = 0.08f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 1.5.dp.toPx()
                )
            }

            // YILDIRIM PATLAMALARI: Merkez kazandan hedeflere arklar
            if (lightningTargets.isNotEmpty()) {
                lightningTargets.forEach { targetIndex ->
                    val r = GameState.getRing(targetIndex)
                    val s = GameState.getSector(targetIndex)
                    val radius = ringRadiiPx[r]
                    val angleRad = (2.0 * PI / GameState.SLOTS_PER_RING) * s - (PI / 2.0)
                    val targetX = (radius * cos(angleRad)).toFloat() + center.x
                    val targetY = (radius * sin(angleRad)).toFloat() + center.y

                    val lightningPath = Path().apply {
                        moveTo(center.x, center.y)
                        val midX1 = center.x + (targetX - center.x) * 0.35f + ((-14..14).random())
                        val midY1 = center.y + (targetY - center.y) * 0.35f + ((-14..14).random())
                        val midX2 = center.x + (targetX - center.x) * 0.70f + ((-14..14).random())
                        val midY2 = center.y + (targetY - center.y) * 0.70f + ((-14..14).random())
                        lineTo(midX1, midY1)
                        lineTo(midX2, midY2)
                        lineTo(targetX, targetY)
                    }

                    drawPath(
                        path = lightningPath,
                        color = Color(0xFFFFEB3B),
                        style = Stroke(width = 7.dp.toPx())
                    )
                    drawPath(
                        path = lightningPath,
                        color = Color.White,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }
        }

        // 2. ORTADAKİ GÜÇ KAZANI (Merkez 2048 Kalbi)
        val cauldronSize = (sizePx * 0.20f).dp
        Box(
            modifier = Modifier
                .size(cauldronSize)
                .scale(if (cauldronCharge >= 1.0f) pulseScale else 1f)
                .shadow(
                    elevation = if (cauldronCharge >= 1.0f) 16.dp else 4.dp,
                    shape = CircleShape,
                    spotColor = Color(0xFFFFEB3B)
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = if (cauldronCharge >= 1.0f) {
                            listOf(Color(0xFFFFEB3B), Color(0xFFFF4081), Color(0xFF651FFF))
                        } else {
                            listOf(
                                Color(0xFFFF80AB).copy(alpha = 0.3f + cauldronCharge * 0.7f),
                                Color(0xFF381358),
                                Color(0xFF140726)
                            )
                        }
                    )
                )
                .border(
                    width = if (cauldronCharge >= 1.0f) 3.5.dp else 2.dp,
                    color = if (cauldronCharge >= 1.0f) Color(0xFFFFEB3B) else Color(0xFFFF80AB),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (cauldronCharge >= 1.0f) "⚡" else "2048",
                    color = Color.White,
                    fontSize = if (cauldronCharge >= 1.0f) 22.sp else 13.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "${(cauldronCharge * 100).toInt()}%",
                    color = Color(0xFFFFD54F),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 3. 3 HALKA X 8 SEKTÖR = 24 ADET 2048 DAİRESEL TAŞ YUVASI
        for (index in 0 until GameState.TOTAL_SLOTS) {
            val r = GameState.getRing(index)
            val s = GameState.getSector(index)
            val radius = ringRadiiPx[r]
            val slotSize = slotSizes[r]

            // Sektör açısı: 0 en tepede (12 yönü) -> -PI/2 ofset
            val angleRad = (2.0 * PI / GameState.SLOTS_PER_RING) * s - (PI / 2.0)
            val xOffsetPx = (radius * cos(angleRad)).toFloat()
            val yOffsetPx = (radius * sin(angleRad)).toFloat()

            val tier = slots.getOrNull(index)
            val isBeingStruck = lightningTargets.contains(index)

            Box(
                modifier = Modifier
                    .offset { IntOffset(xOffsetPx.roundToInt(), yOffsetPx.roundToInt()) }
                    .size(slotSize)
                    .scale(if (isBeingStruck) 1.25f else 1f)
                    .shadow(
                        elevation = if (tier != null) 6.dp else 1.dp,
                        shape = CircleShape,
                        spotColor = tier?.color ?: Color.Transparent
                    )
                    .clip(CircleShape)
                    .background(
                        when {
                            isBeingStruck -> Color(0xFFFFEB3B)
                            tier != null -> tier.color
                            else -> Color(0xFF231A3D).copy(alpha = 0.8f)
                        }
                    )
                    .border(
                        width = if (isBeingStruck) 3.dp else if (tier != null) 2.dp else 1.dp,
                        color = when {
                            isBeingStruck -> Color.White
                            tier != null -> Color.White.copy(alpha = 0.8f)
                            else -> Color(0xFF45366D)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (tier != null) {
                    Text(
                        text = if (isBeingStruck) "💥" else "${tier.value}",
                        color = tier.textColor,
                        fontSize = when {
                            tier.value >= 1024 -> 10.sp
                            tier.value >= 128 -> 12.sp
                            tier.value >= 16 -> 14.sp
                            else -> 16.sp
                        },
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
