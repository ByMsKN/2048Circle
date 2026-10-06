package com.alchemist.circle.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alchemist.circle.model.ElementTier
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun CircularBoard(
    slots: List<ElementTier?>,
    rotationAngle: Float,
    isHammerActive: Boolean,
    onSlotClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedRotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "BoardRotation"
    )

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val sizePx = constraints.maxWidth.coerceAtMost(constraints.maxHeight).toFloat()
        val radiusPx = sizePx * 0.38f
        val slotSize = (sizePx * 0.18f).dp

        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF2E1A47).copy(alpha = 0.5f), Color.Transparent),
                    center = center,
                    radius = radiusPx * 1.3f
                ),
                radius = radiusPx * 1.3f,
                center = center
            )

            drawCircle(
                color = Color(0xFF673AB7).copy(alpha = 0.35f),
                radius = radiusPx,
                center = center,
                style = Stroke(width = 4.dp.toPx())
            )

            drawCircle(
                color = Color(0xFFFFD54F).copy(alpha = 0.2f),
                radius = radiusPx * 0.5f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        val count = slots.size
        for (index in 0 until count) {
            val baseAngleRad = (2.0 * PI / count) * index
            val totalAngleRad = baseAngleRad + Math.toRadians(animatedRotation.toDouble())

            val xOffsetPx = (radiusPx * cos(totalAngleRad)).toFloat()
            val yOffsetPx = (radiusPx * sin(totalAngleRad)).toFloat()

            val tier = slots[index]

            Box(
                modifier = Modifier
                    .offset { IntOffset(xOffsetPx.roundToInt(), yOffsetPx.roundToInt()) }
                    .size(slotSize)
                    .clip(CircleShape)
                    .background(
                        if (tier != null) tier.color.copy(alpha = 0.85f)
                        else Color(0xFF1E1B2E).copy(alpha = 0.6f)
                    )
                    .border(
                        width = if (isHammerActive && tier != null) 3.dp else 1.5.dp,
                        color = when {
                            isHammerActive && tier != null -> Color(0xFFFF1744)
                            tier != null -> tier.glowColor
                            else -> Color(0xFF3F3B59)
                        },
                        shape = CircleShape
                    )
                    .clickable { onSlotClick(index) },
                contentAlignment = Alignment.Center
            ) {
                if (tier != null) {
                    Text(
                        text = tier.symbol,
                        fontSize = 24.sp,
                        modifier = Modifier.rotate(-animatedRotation)
                    )
                }
            }
        }
    }
}
