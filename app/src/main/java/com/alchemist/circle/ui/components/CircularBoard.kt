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

        // TEK ÇEMBER MÜKEMMEL ALTIN ORAN:
        // Yarıçap (radiusPx) = %37
        // Yuva boyutu (slotSize) = 54dp
        // 8 yuva (45 derece simetrik aralık)
        // İki yuva arası ferah net boşluk: ~40dp (asla temas etmez, çok rahat okunur)
        val radiusPx = sizePx * 0.37f
        val slotSize = 54.dp
        val count = GameState.SLOT_COUNT

        // 1. NEŞELİ 2048 YÖRÜNGE RAYI & ELEKTRİK EFEKTLERİ CANVAS
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)

            // Dış atmosferik sihir halesi
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF7C4DFF).copy(alpha = magicGlow * 0.30f),
                        Color(0xFFFF4081).copy(alpha = magicGlow * 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radiusPx * 1.35f
                ),
                radius = radiusPx * 1.35f,
                center = center
            )

            // Ana Yörünge Rayı (Neon Çember)
            drawCircle(
                color = Color(0xFFFFD54F).copy(alpha = 0.45f),
                radius = radiusPx,
                center = center,
                style = Stroke(width = 4.dp.toPx())
            )

            // İç Dekoratif Kesikli Çember
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.25f),
                radius = radiusPx * 0.65f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Üst Portal Işıltısı (Yeni Taş Girişi - Saat 12 yönü)
            val portalY = center.y - radiusPx
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.75f),
                radius = 8.dp.toPx(),
                center = Offset(center.x, portalY)
            )

            // YILDIRIM PATLAMALARI: Merkez kazandan hedeflere arklar
            if (lightningTargets.isNotEmpty()) {
                lightningTargets.forEach { targetIndex ->
                    val angleRad = (2.0 * PI / count) * targetIndex - (PI / 2.0)
                    val targetX = (radiusPx * cos(angleRad)).toFloat() + center.x
                    val targetY = (radiusPx * sin(angleRad)).toFloat() + center.y

                    val lightningPath = Path().apply {
                        moveTo(center.x, center.y)
                        val midX1 = center.x + (targetX - center.x) * 0.35f + ((-16..16).random())
                        val midY1 = center.y + (targetY - center.y) * 0.35f + ((-16..16).random())
                        val midX2 = center.x + (targetX - center.x) * 0.70f + ((-16..16).random())
                        val midY2 = center.y + (targetY - center.y) * 0.70f + ((-16..16).random())
                        lineTo(midX1, midY1)
                        lineTo(midX2, midY2)
                        lineTo(targetX, targetY)
                    }

                    drawPath(
                        path = lightningPath,
                        color = Color(0xFFFFEB3B),
                        style = Stroke(width = 8.dp.toPx())
                    )
                    drawPath(
                        path = lightningPath,
                        color = Color.White,
                        style = Stroke(width = 3.5.dp.toPx())
                    )
                }
            }
        }

        // 2. ORTADAKİ GÜÇ KAZANI (Merkez 2048 Kalbi)
        val cauldronSize = 82.dp
        Box(
            modifier = Modifier
                .size(cauldronSize)
                .scale(if (cauldronCharge >= 1.0f) pulseScale else 1f)
                .shadow(
                    elevation = if (cauldronCharge >= 1.0f) 16.dp else 5.dp,
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
                                Color(0xFFFF80AB).copy(alpha = 0.35f + cauldronCharge * 0.65f),
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
                    fontSize = if (cauldronCharge >= 1.0f) 28.sp else 16.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "${(cauldronCharge * 100).toInt()}%",
                    color = Color(0xFFFFD54F),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 3. 8 ADET GENİŞ, OKUNAKLI 2048 TAŞ YUVASI (Saat 12 yönünden 45° aralıklarla)
        for (index in 0 until count) {
            val angleRad = (2.0 * PI / count) * index - (PI / 2.0)
            val xOffsetPx = (radiusPx * cos(angleRad)).toFloat()
            val yOffsetPx = (radiusPx * sin(angleRad)).toFloat()

            val tier = slots.getOrNull(index)
            val isBeingStruck = lightningTargets.contains(index)

            Box(
                modifier = Modifier
                    .offset { IntOffset(xOffsetPx.roundToInt(), yOffsetPx.roundToInt()) }
                    .size(slotSize)
                    .scale(if (isBeingStruck) 1.25f else 1f)
                    .shadow(
                        elevation = if (tier != null) 8.dp else 2.dp,
                        shape = CircleShape,
                        spotColor = tier?.color ?: Color.Transparent
                    )
                    .clip(CircleShape)
                    .background(
                        when {
                            isBeingStruck -> Color(0xFFFFEB3B)
                            tier != null -> tier.color
                            else -> Color(0xFF261D42).copy(alpha = 0.85f)
                        }
                    )
                    .border(
                        width = if (isBeingStruck) 3.5.dp else if (tier != null) 2.5.dp else 1.5.dp,
                        color = when {
                            isBeingStruck -> Color.White
                            tier != null -> Color.White.copy(alpha = 0.85f)
                            else -> Color(0xFF4C3E75)
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
                            tier.value >= 1024 -> 13.sp
                            tier.value >= 128 -> 15.sp
                            tier.value >= 16 -> 18.sp
                            else -> 20.sp
                        },
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
