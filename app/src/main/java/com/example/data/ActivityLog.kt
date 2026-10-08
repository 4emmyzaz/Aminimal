package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Supported activity categories that count toward the daily streak.
 */
enum class ActivityType {
    HABIT,
    NOTE
}

/**
 * Tracks daily user completions (either completing a habit or creating/editing a note).
 * Multiple activities on the same calendar day are registered in logs, but idempotently
 * count as 1 active day for the streak.
 */
@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "activity_type")
    val activityType: ActivityType,

    @ColumnInfo(name = "date")
    val date: String, // ISO-8601 "YYYY-MM-DD"

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "details")
    val details: String = ""
)
