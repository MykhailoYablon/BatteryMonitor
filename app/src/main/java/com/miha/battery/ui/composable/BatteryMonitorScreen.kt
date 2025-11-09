package com.miha.battery.ui.composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.miha.battery.data.BatteryInfo
import com.miha.battery.entity.BatteryEvent
import com.miha.battery.model.BatteryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryMonitorScreen(viewModel: BatteryViewModel) {
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val events by viewModel.recentEvents.collectAsState()
    val stats by viewModel.stats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Battery Monitor") },
                actions = {
                    TextButton(onClick = { viewModel.clearHistory() }) {
                        Text("Clear")
                    }
                }
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
            item { Text("Recent Events", style = MaterialTheme.typography.titleLarge) }
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
            Text(
                text = "${info.level}%",
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = if (info.isCharging) "Charging" else "Discharging",
                style = MaterialTheme.typography.titleMedium
            )
            Divider()
            InfoRow("Temperature", "${info.temperature}°C")
            InfoRow("Voltage", "${info.voltage} mV")
            InfoRow("capacity", "${info.capacity} mV")
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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