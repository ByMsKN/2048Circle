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
import kotlin.math.*

@Composable
fun CircularBoard(
    slots: List<ElementTier?>,
    cauldronCharge: Float,
    lightningTargets: List<Int>,
    modifier: Modifier = Modifier
) {
    // Güç kazanı ve yıldırım için neşeli nabız (pulse) efekti
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

    // Arka plan sihirli ışıltısı
    val magicGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
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
        // Matematiksel kesinlik:
        // Yarıçap (radiusPx): %41
        // Daire yuva boyutu (slotSize): %11 (~40dp)
        // Çember çevresi (2 * PI * R) ≈ 2.57 * sizePx.
        // 10 daire toplamda: 10 * 0.11 = 1.10 * sizePx kaplar.
        // Kalan boşluk: 1.47 * sizePx!
        // Daireler arasında çaplarının 1.3 katı kadar bol ve ferah BOŞLUK kalır, KESİNLİKLE BİRBİRİNE DEĞMEZ!
        val radiusPx = sizePx * 0.41f
        val slotSize = (sizePx * 0.11f).dp
        val count = slots.size

        // 1. NEŞELİ SİHİR RAYI VE YILDIRIM EFEKTLERİ CANVAS
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)

            // Tatlı sihirli renkli arka plan halesi
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF7C4DFF).copy(alpha = magicGlow * 0.35f),
                        Color(0xFFFF4081).copy(alpha = magicGlow * 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radiusPx * 1.35f
                ),
                radius = radiusPx * 1.35f,
                center = center
            )

            // Renkli Neon Yörünge Rayı (Candy Track)
            drawCircle(
                color = Color(0xFFFFD54F).copy(alpha = 0.45f),
                radius = radiusPx,
                center = center,
                style = Stroke(width = 4.dp.toPx())
            )

            // YILDIRIM PATLAMALARI: Merkez kazandan hedeflere renkli elektrik arkları
            if (lightningTargets.isNotEmpty()) {
                lightningTargets.forEach { targetIndex ->
                    val angleRad = (2.0 * PI / count) * targetIndex
                    val targetX = (radiusPx * cos(angleRad)).toFloat() + center.x
                    val targetY = (radiusPx * sin(angleRad)).toFloat() + center.y

                    val lightningPath = Path().apply {
                        moveTo(center.x, center.y)
                        val midX1 = center.x + (targetX - center.x) * 0.35f + ((-18..18).random())
                        val midY1 = center.y + (targetY - center.y) * 0.35f + ((-18..18).random())
                        val midX2 = center.x + (targetX - center.x) * 0.70f + ((-18..18).random())
                        val midY2 = center.y + (targetY - center.y) * 0.70f + ((-18..18).random())
                        lineTo(midX1, midY1)
                        lineTo(midX2, midY2)
                        lineTo(targetX, targetY)
                    }

                    // Dış elektrik halesi (Sarı/Camgöbeği)
                    drawPath(
                        path = lightningPath,
                        color = Color(0xFFFFEB3B),
                        style = Stroke(width = 8.dp.toPx())
                    )
                    // İç parlak beyaz çekirdek
                    drawPath(
                        path = lightningPath,
                        color = Color.White,
                        style = Stroke(width = 3.5.dp.toPx())
                    )
                }
            }
        }

        // 2. ORTADAKİ RENKLİ GÜÇ KAZANI (Candy Magic Cauldron)
        val cauldronSize = (sizePx * 0.29f).dp
        Box(
            modifier = Modifier
                .size(cauldronSize)
                .scale(if (cauldronCharge >= 1.0f) pulseScale else 1f)
                .shadow(
                    elevation = if (cauldronCharge >= 1.0f) 16.dp else 6.dp,
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
                                Color(0xFFFF80AB).copy(alpha = 0.4f + cauldronCharge * 0.6f),
                                Color(0xFF4A148C),
                                Color(0xFF1A0933)
                            )
                        }
                    )
                )
                .border(
                    width = if (cauldronCharge >= 1.0f) 4.dp else 2.5.dp,
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
                    text = if (cauldronCharge >= 1.0f) "⚡" else "🧪",
                    fontSize = 32.sp
                )
                Text(
                    text = "${(cauldronCharge * 100).toInt()}%",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // 3. YÖRÜNGEDEKİ 10 ADET YUVA (BÜYÜK İKONLAR & PARLAK RENKLER)
        for (index in 0 until count) {
            // Sabit trigonometrik yuva konumu: taşlar dönme yönünde yuvalar arasında hareket eder!
            val angleRad = (2.0 * PI / count) * index
            val xOffsetPx = (radiusPx * cos(angleRad)).toFloat()
            val yOffsetPx = (radiusPx * sin(angleRad)).toFloat()

            val tier = slots[index]
            val isBeingStruck = lightningTargets.contains(index)

            Box(
                modifier = Modifier
                    .offset { IntOffset(xOffsetPx.roundToInt(), yOffsetPx.roundToInt()) }
                    .size(slotSize)
                    .scale(if (isBeingStruck) 1.3f else if (tier != null) 1.05f else 1f)
                .shadow(
                    elevation = if (tier != null) 8.dp else 0.dp,
                    shape = CircleShape,
                    spotColor = tier?.color ?: Color.Transparent
                )
                .clip(CircleShape)
                .background(
                    when {
                        isBeingStruck -> Color(0xFFFFEB3B)
                        tier != null -> tier.color
                        else -> Color(0xFF261D42).copy(alpha = 0.7f)
                    }
                )
                .border(
                    width = if (isBeingStruck) 3.5.dp else if (tier != null) 2.5.dp else 1.2.dp,
                    color = when {
                        isBeingStruck -> Color.White
                        tier != null -> tier.borderColor
                        else -> Color(0xFF4C3E75)
                    },
                    shape = CircleShape
                ),
                contentAlignment = Alignment.Center
            ) {
                if (tier != null) {
                    Text(
                        text = if (isBeingStruck) "💥" else tier.symbol,
                        fontSize = 24.sp // Büyütülmüş canlı emoji boyutu
                    )
                }
            }
        }
    }
}
