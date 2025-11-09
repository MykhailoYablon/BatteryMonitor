package com.miha.battery.dao


import com.miha.battery.entity.BatteryEvent
import kotlinx.coroutines.flow.Flow

class BatteryRepository(private val dao: BatteryEventDao) {
    fun getRecentEvents(): Flow<List<BatteryEvent>> = dao.getRecentEvents()

    suspend fun insertEvent(event: BatteryEvent) {
        dao.insertEvent(event)
    }

    suspend fun deleteAllEvents() {
        dao.deleteAllEvents()
    }

    suspend fun getChargingCount(): Int = dao.getChargingCount()

    suspend fun getDischargingCount(): Int = dao.getDischargingCount()
}