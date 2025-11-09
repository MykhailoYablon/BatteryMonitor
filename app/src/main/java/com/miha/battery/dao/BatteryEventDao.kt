package com.miha.battery.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.miha.battery.entity.BatteryEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface BatteryEventDao {
    @Insert
    suspend fun insertEvent(event: BatteryEvent)

    @Query("SELECT * FROM battery_events ORDER BY timestamp DESC LIMIT 50")
    fun getRecentEvents(): Flow<List<BatteryEvent>>

    @Query("SELECT * FROM battery_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<BatteryEvent>>

    @Query("DELETE FROM battery_events")
    suspend fun deleteAllEvents()

    @Query("SELECT COUNT(*) FROM battery_events WHERE eventType = 'CHARGING'")
    suspend fun getChargingCount(): Int

    @Query("SELECT COUNT(*) FROM battery_events WHERE eventType = 'DISCHARGING'")
    suspend fun getDischargingCount(): Int
}