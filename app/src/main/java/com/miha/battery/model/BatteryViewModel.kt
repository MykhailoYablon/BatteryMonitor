package com.miha.battery.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.miha.battery.dao.BatteryDatabase
import com.miha.battery.dao.BatteryRepository
import com.miha.battery.data.BatteryInfo
import com.miha.battery.entity.BatteryEvent
import com.miha.battery.service.BatteryMonitorService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BatteryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BatteryRepository
    private val batteryService = BatteryMonitorService(application)

    val batteryInfo: StateFlow<BatteryInfo> = batteryService.batteryInfo
    val recentEvents: StateFlow<List<BatteryEvent>>

    private val _stats = MutableStateFlow(Pair(0, 0)) // charging, discharging
    val stats: StateFlow<Pair<Int, Int>> = _stats

    private var lastChargingState = false

    init {
        val dao = BatteryDatabase.getDatabase(application).batteryEventDao()
        repository = BatteryRepository(dao)
        recentEvents = MutableStateFlow(emptyList())

        viewModelScope.launch {
            repository.getRecentEvents().collect { events ->
                (recentEvents as MutableStateFlow).value = events
            }
        }

        viewModelScope.launch {
            batteryInfo.collect { info ->
                if (info.isCharging != lastChargingState) {
                    logBatteryEvent(info)
                    lastChargingState = info.isCharging
                    updateStats()
                }
            }
        }

        batteryService.startMonitoring()
        updateStats()
    }

    private fun logBatteryEvent(info: BatteryInfo) {
        viewModelScope.launch {
            val event = BatteryEvent(
                timestamp = System.currentTimeMillis(),
                eventType = if (info.isCharging) "CHARGING" else "DISCHARGING",
                batteryLevel = info.level,
                temperature = info.temperature,
                voltage = info.voltage,
                capacity = info.capacity,
                health = info.health
            )
            repository.insertEvent(event)
        }
    }

    private fun updateStats() {
        viewModelScope.launch {
            val charging = repository.getChargingCount()
            val discharging = repository.getDischargingCount()
            _stats.value = Pair(charging, discharging)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.deleteAllEvents()
            updateStats()
        }
    }

    override fun onCleared() {
        super.onCleared()
        batteryService.stopMonitoring()
    }
}
