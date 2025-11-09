package com.miha.battery.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "battery_events")
data class BatteryEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val eventType: String, // "CHARGING", "DISCHARGING", "FULL", "UNPLUGGED"
    val batteryLevel: Int,
    val temperature: Float,
    val voltage: Int,
    val health: String,
    val chargeCounter: Int = 0, // in μAh
    val currentCapacity: Int = 0 // in μAh
)