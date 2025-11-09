package com.miha.battery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miha.battery.ui.BatteryMonitorScreen
import com.miha.battery.ui.theme.BatteryMonitorTheme
import com.miha.battery.viewmodel.BatteryViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BatteryMonitorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: BatteryViewModel = viewModel()
                    BatteryMonitorScreen(viewModel)
                }
            }
        }
    }
}