package com.miha.battery.ui.composable

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.miha.battery.data.BatteryInfo
import com.miha.battery.entity.BatteryEvent
import com.miha.battery.model.BatteryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryMonitorScreen(viewModel: BatteryViewModel) {
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val events by viewModel.recentEvents.collectAsState()
    val stats by viewModel.stats.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Battery Monitor") },
//                actions = {
//                    TextButton(onClick = { viewModel.clearHistory() }) {
//                        Text("Clear")
//                    }
//                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { CurrentBatteryCard(batteryInfo) }
            item { StatsCard(stats) }
            item {
                Text(
                    "Recent Events",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
            }
            items(events) { event ->
                EventCard(event)
            }
        }
    }
}

@Composable
fun BatteryVisual(
    level: Int,
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

    val batteryColor = when {
        level <= 20 -> Color(0xFFEF5350)
        level <= 50 -> lerp(
            Color(0xFFFFA726), // Red at 0%
            Color(0xFFFFEE58), // Green at 100%
            level / 100f
        )

        level <= 80 -> lerp(
            Color(0xFFFFEE58), // Red at 0%
            Color(0xFF00DC0C), // Green at 100%
            level / 100f
        )

        else -> Color(0xFF00DC0C)
    }

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


@Composable
fun CurrentBatteryCard(info: BatteryInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (info.isCharging)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Animated Battery Visualization
            BatteryVisual(
                level = info.level,
                isCharging = info.isCharging,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Text(
                text = "${info.level}%",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = if (info.isCharging) "Charging" else "Discharging",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Divider()

            // Capacity Information
            if (info.chargeCounter > 0) {
                Text(
                    text = "Battery Capacity",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
                InfoRow("Current Charge", "${info.chargeCounter / 1000} mAh")
                InfoRow(
                    "Estimated Total",
                    "${(info.chargeCounter / (info.level / 100f) / 1000).toInt()} mAh"
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Other Information
            InfoRow("Temperature", "${info.temperature}°C")
            InfoRow("Voltage", "${info.voltage} mV")
            InfoRow("Health", info.health)
            InfoRow("Technology", info.technology)
        }
    }
}

@Composable
fun StatsCard(stats: Pair<Int, Int>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatColumn("Charging Events", stats.first)
            StatColumn("Discharging Events", stats.second)
        }
    }
}

@Composable
fun StatColumn(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count.toString(), style = MaterialTheme.typography.headlineMedium)
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun EventCard(event: BatteryEvent) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = event.eventType,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = formatTimestamp(event.timestamp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "${event.batteryLevel}%")
                    Text(
                        text = "${event.temperature}°C",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (event.chargeCounter > 0) {
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Charge: ${event.chargeCounter / 1000} mAh",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "${event.voltage} mV",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
