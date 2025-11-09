package com.miha.battery.data

data class BatteryInfo(
    val level: Int = 0,
    val isCharging: Boolean = false,
    val temperature: Float = 0f,
    val voltage: Int = 0,
    val health: String = "Unknown",
    val technology: String = "Unknown",
    val currentCapacity: Int = 0, // in microampere-hours (μAh)
    val chargeCounter: Int = 0, // current charge in μAh
    val capacityPercent: Float = 0f // current capacity as % of design capacity
)