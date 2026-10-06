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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
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
    rotationAngle: Float,
    cauldronCharge: Float,
    lightningTargets: List<Int>,
    modifier: Modifier = Modifier
) {
    val animatedRotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "BoardRotation"
    )

    // Kazan doluluğu ve yıldırım için nabız (pulse) efekti
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val sizePx = constraints.maxWidth.coerceAtMost(constraints.maxHeight).toFloat()
        // 10 dairenin birbirine çarpmaması için ideal oranlar:
        // Yarıçap: %40, Daire boyutu: %14 (Çapı ~48-52dp)
        // Çevre = 2 * PI * R ≈ 2.5 * sizePx. 10 daire toplamda ~1.4 * sizePx kaplar -> Boşluklar ferah kalır!
        val radiusPx = sizePx * 0.40f
        val slotSize = (sizePx * 0.14f).dp

        // 1. ARKA PLAN YÖRÜNGESİ & YILDIRIM EFEKTLERİ CANVAS
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)

            // Dış atmosferik gölge
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF2E1A47).copy(alpha = 0.4f), Color.Transparent),
                    center = center,
                    radius = radiusPx * 1.25f
                ),
                radius = radiusPx * 1.25f,
                center = center
            )

            // Ana Yörünge Rayı
            drawCircle(
                color = Color(0xFF673AB7).copy(alpha = 0.35f),
                radius = radiusPx,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )

            // YILDIRIM ÇİZGİLERİ: Merkezden hedeflere elektrik arkları
            if (lightningTargets.isNotEmpty()) {
                val count = slots.size
                lightningTargets.forEach { targetIndex ->
                    val baseAngleRad = (2.0 * PI / count) * targetIndex
                    val totalAngleRad = baseAngleRad + Math.toRadians(animatedRotation.toDouble())
                    val targetX = (radiusPx * cos(totalAngleRad)).toFloat() + center.x
                    val targetY = (radiusPx * sin(totalAngleRad)).toFloat() + center.y

                    // Zikzak yıldırım yolu
                    val lightningPath = Path().apply {
                        moveTo(center.x, center.y)
                        val midX1 = center.x + (targetX - center.x) * 0.33f + ((-15..15).random())
                        val midY1 = center.y + (targetY - center.y) * 0.33f + ((-15..15).random())
                        val midX2 = center.x + (targetX - center.x) * 0.66f + ((-15..15).random())
                        val midY2 = center.y + (targetY - center.y) * 0.66f + ((-15..15).random())
                        lineTo(midX1, midY1)
                        lineTo(midX2, midY2)
                        lineTo(targetX, targetY)
                    }

                    // Dış mavi/mor elektrik halesi
                    drawPath(
                        path = lightningPath,
                        color = Color(0xFF00E5FF),
                        style = Stroke(width = 6.dp.toPx())
                    )
                    // İç beyaz çekirdek yıldırım
                    drawPath(
                        path = lightningPath,
                        color = Color.White,
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }
            }
        }

        // 2. MERKEZDEKİ GÜÇ KAZANI (Cauldron)
        val cauldronSize = (sizePx * 0.28f).dp
        Box(
            modifier = Modifier
                .size(cauldronSize)
                .scale(if (cauldronCharge >= 1.0f) pulseScale else 1f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = if (cauldronCharge >= 1.0f) {
                            listOf(Color(0xFF00E5FF), Color(0xFF651FFF), Color(0xFF1A0A3A))
                        } else {
                            listOf(
                                Color(0xFFFFD54F).copy(alpha = 0.2f + cauldronCharge * 0.6f),
                                Color(0xFF311B92).copy(alpha = 0.8f),
                                Color(0xFF130924)
                            )
                        }
                    )
                )
                .border(
                    width = if (cauldronCharge >= 1.0f) 3.5.dp else 2.dp,
                    color = if (cauldronCharge >= 1.0f) Color(0xFF00E5FF) else Color(0xFFFFD54F).copy(alpha = 0.4f + cauldronCharge * 0.6f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (cauldronCharge >= 1.0f) "⚡" else "🔮",
                    fontSize = 28.sp
                )
                Text(
                    text = "${(cauldronCharge * 100).toInt()}%",
                    color = if (cauldronCharge >= 1.0f) Color(0xFF00E5FF) else Color(0xFFFFD54F),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 3. YÖRÜNGEDEKİ 10 ADET YUVA (SLOT)
        val count = slots.size
        for (index in 0 until count) {
            val baseAngleRad = (2.0 * PI / count) * index
            val totalAngleRad = baseAngleRad + Math.toRadians(animatedRotation.toDouble())

            val xOffsetPx = (radiusPx * cos(totalAngleRad)).toFloat()
            val yOffsetPx = (radiusPx * sin(totalAngleRad)).toFloat()

            val tier = slots[index]
            val isBeingStruck = lightningTargets.contains(index)

            Box(
                modifier = Modifier
                    .offset { IntOffset(xOffsetPx.roundToInt(), yOffsetPx.roundToInt()) }
                    .size(slotSize)
                    .scale(if (isBeingStruck) 1.25f else 1f)
                    .clip(CircleShape)
                    .background(
                        when {
                            isBeingStruck -> Color(0xFF00E5FF) // Yıldırım çarptığında parıldasın
                            tier != null -> tier.color.copy(alpha = 0.9f)
                            else -> Color(0xFF1E1B2E).copy(alpha = 0.65f)
                        }
                    )
                    .border(
                        width = if (isBeingStruck) 3.5.dp else 1.5.dp,
                        color = when {
                            isBeingStruck -> Color.White
                            tier != null -> tier.glowColor
                            else -> Color(0xFF433D61)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (tier != null) {
                    Text(
                        text = if (isBeingStruck) "💥" else tier.symbol,
                        fontSize = 20.sp,
                        modifier = Modifier.rotate(-animatedRotation)
                    )
                }
            }
        }
    }
}
