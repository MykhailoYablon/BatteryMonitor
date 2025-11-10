package com.miha.battery.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
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
//            item {
//                IconButton(onClick = { viewModel.clearHistory() }) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.brush),
//                        contentDescription = "Clear History",
//                        tint = Color(0xFF3AAB3E)
//                    )
//                }
//            }
            items(events) { event ->
                EventCard(event)
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
            val level = info.level
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
            // Animated Battery Visualization
            BatteryVisual(
                level = info.level,
                batteryColor = batteryColor,
                isCharging = info.isCharging,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Text(
                text = "${info.level}%",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = batteryColor
            )
            Text(
                text = if (info.isCharging) "Charging" else "Discharging",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = if (info.isCharging) Color.Green else Color(0xFFFF5722)
            )
            HorizontalDivider()

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

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
            )

            // Other Information
            InfoRow("Temperature", "${info.temperature}°C")
            InfoRow("Voltage", "${info.voltage} mV")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Health", style = MaterialTheme.typography.bodyMedium)
                Text(text = info.health, style = MaterialTheme.typography.bodyMedium,
                    color = Color.Green)
            }
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
