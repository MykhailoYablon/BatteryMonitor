package com.miha.battery.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.miha.battery.data.BatteryInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class BatteryMonitorService(private val context: Context) {
    private val _batteryInfo = MutableStateFlow(BatteryInfo())
    val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let { updateBatteryInfo(it) }
        }
    }

    fun startMonitoring() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, filter)
    }

    fun stopMonitoring() {
        context.unregisterReceiver(batteryReceiver)
    }

    private fun updateBatteryInfo(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = (level / scale.toFloat() * 100).toInt()

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)

        val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
        val healthStr = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }

        val tech = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"

        // Get capacity information using BatteryManager
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val chargeCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        val capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        // Get design capacity (total capacity) - API 28+
        val currentCapacity = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        } else {
            0
        }

        // Calculate capacity percentage (chargeCounter relative to typical battery capacity)
        val capacityPercent = if (chargeCounter > 0) {
            (chargeCounter.toFloat() / 3000000f) * 100f // Assuming ~3000mAh typical battery
        } else {
            0f
        }

        _batteryInfo.value = BatteryInfo(
            level = batteryPct,
            isCharging = isCharging,
            temperature = temp,
            voltage = voltage,
            health = healthStr,
            technology = tech,
            currentCapacity = currentCapacity,
            chargeCounter = chargeCounter,
            capacityPercent = capacityPercent
        )
    }
}