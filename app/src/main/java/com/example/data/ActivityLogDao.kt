package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
    @Insert
    suspend fun insertLog(log: ActivityLog): Long

    @Query("SELECT * FROM activity_logs WHERE date = :date ORDER BY timestamp DESC")
    fun getLogsForDate(date: String): Flow<List<ActivityLog>>

    @Query("SELECT COUNT(*) FROM activity_logs WHERE date = :date")
    suspend fun countActivitiesOnDate(date: String): Int

    @Query("SELECT COUNT(DISTINCT date) FROM activity_logs")
    suspend fun countDistinctActiveDays(): Int

    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 30): Flow<List<ActivityLog>>

    @Query("DELETE FROM activity_logs")
    suspend fun clear()
}
