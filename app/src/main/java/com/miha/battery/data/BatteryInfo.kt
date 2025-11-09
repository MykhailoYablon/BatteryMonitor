package com.miha.battery.data

data class BatteryInfo(
    val level: Int = 0,
    val isCharging: Boolean = false,
    val temperature: Float = 0f,
    val voltage: Int = 0,
    val capacity: Int = 0,
    val health: String = "Unknown",
    val technology: String = "Unknown"
)