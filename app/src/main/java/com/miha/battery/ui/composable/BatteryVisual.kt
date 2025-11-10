package com.miha.battery.ui.composable

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun BatteryVisual(
    level: Int,
    batteryColor: Color,
    isCharging: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedLevel by animateFloatAsState(
        targetValue = level / 100f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "batteryLevel"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "charging")
    val chargingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chargingAlpha"
    )


//    val batteryColor = lerp(
//        Color(0xFFEF5350), // Red at 0%
//        Color(0xFF02B90D), // Green at 100%
//        level / 100f
//    )

    val chargingColor = Color(0xFF42A5F5)

    Box(
        modifier = modifier
            .width(200.dp)
            .height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val batteryWidth = size.width * 0.85f
            val batteryHeight = size.height * 0.7f
            val batteryX = (size.width - batteryWidth) / 2
            val batteryY = (size.height - batteryHeight) / 2

            val tipWidth = size.width * 0.05f
            val tipHeight = batteryHeight * 0.4f
            val tipX = batteryX + batteryWidth
            val tipY = batteryY + (batteryHeight - tipHeight) / 2

            drawRoundRect(
                color = Color.Gray,
                topLeft = Offset(tipX, tipY),
                size = Size(tipWidth, tipHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )

            drawRoundRect(
                color = Color.Gray,
                topLeft = Offset(batteryX, batteryY),
                size = Size(batteryWidth, batteryHeight),
                cornerRadius = CornerRadius(12f, 12f),
                style = Stroke(width = 6f)
            )

            val padding = 8f
            val fillWidth = (batteryWidth - padding * 2) * animatedLevel
            val fillHeight = batteryHeight - padding * 2
            val fillX = batteryX + padding
            val fillY = batteryY + padding

            if (fillWidth > 0) {
                val fillColor = if (isCharging) chargingColor else batteryColor
                val alpha = if (isCharging) chargingAlpha else 1f

                val gradient = Brush.verticalGradient(
                    colors = listOf(
                        fillColor.copy(alpha = alpha),
                        fillColor.copy(alpha = alpha * 0.7f)
                    )
                )

                drawRoundRect(
                    brush = gradient,
                    topLeft = Offset(fillX, fillY),
                    size = Size(fillWidth, fillHeight),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }

            if (isCharging) {
                val boltColor = Color.White.copy(alpha = chargingAlpha)
                val centerX = batteryX + batteryWidth / 2
                val centerY = batteryY + batteryHeight / 2
                val boltSize = batteryHeight * 0.4f

                drawLine(
                    color = boltColor,
                    start = Offset(centerX - boltSize * 0.2f, centerY - boltSize * 0.5f),
                    end = Offset(centerX + boltSize * 0.1f, centerY),
                    strokeWidth = 8f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = boltColor,
                    start = Offset(centerX + boltSize * 0.1f, centerY),
                    end = Offset(centerX - boltSize * 0.1f, centerY),
                    strokeWidth = 8f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = boltColor,
                    start = Offset(centerX - boltSize * 0.1f, centerY),
                    end = Offset(centerX + boltSize * 0.2f, centerY + boltSize * 0.5f),
                    strokeWidth = 8f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}