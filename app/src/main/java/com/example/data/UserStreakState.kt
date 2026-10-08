package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores the user's current streak progress, safety net freezes, and active dates.
 * Modeled after the Elevate app streak and safety net architecture.
 */
@Entity(tableName = "user_streak_state")
data class UserStreakState(
    @PrimaryKey
    val id: Int = 1,

    @ColumnInfo(name = "current_streak")
    val currentStreak: Int = 0,

    @ColumnInfo(name = "streak_freezes_count")
    val streakFreezesCount: Int = DEFAULT_INITIAL_FREEZES,

    @ColumnInfo(name = "last_active_date")
    val lastActiveDate: String? = null, // Stored as ISO-8601 "YYYY-MM-DD"

    @ColumnInfo(name = "freeze_last_used_date")
    val freezeLastUsedDate: String? = null, // Stored as ISO-8601 "YYYY-MM-DD"

    @ColumnInfo(name = "post_reset_streak_days")
    val postResetStreakDays: Int = 0, // Counts consecutive days post-reset to trigger freeze recovery

    @ColumnInfo(name = "coins")
    val coins: Int = 100, // In-app rewards currency to purchase freezes

    @ColumnInfo(name = "highest_streak")
    val highestStreak: Int = 0
) {
    companion object {
        const val MAX_FREEZES = 2
        const val DEFAULT_INITIAL_FREEZES = 2
        const val FREEZE_COIN_COST = 50
    }
}
