package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Book
import androidx.compose.ui.graphics.vector.ImageVector

data class AchievementBadge(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isUnlocked: Boolean,
    val requiredCriteria: String
)

class BadgesRepository {

    /**
     * Evaluates badges dynamically against the current streak state and activity history.
     */
    fun getBadges(streakState: UserStreakState, recentLogs: List<ActivityLog>): List<AchievementBadge> {
        val currentStreak = streakState.currentStreak
        val highestStreak = maxOf(streakState.highestStreak, currentStreak)
        val hasCompletedBefore9Am = recentLogs.any { log ->
            // Check if log timestamp hour is before 9 AM
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = log.timestamp }
            cal.get(java.util.Calendar.HOUR_OF_DAY) < 9
        }
        val noteLogsCount = recentLogs.count { it.activityType == ActivityType.NOTE }
        val usedFreeze = streakState.freezeLastUsedDate != null

        return listOf(
            AchievementBadge(
                id = "first_spark",
                title = "First Spark",
                description = "Began your journey by logging your first active day.",
                icon = Icons.Filled.Bolt,
                isUnlocked = highestStreak >= 1,
                requiredCriteria = "1 day streak"
            ),
            AchievementBadge(
                id = "early_bird",
                title = "Early Bird",
                description = "Completed a habit or wrote a note before 9:00 AM.",
                icon = Icons.Filled.WbSunny,
                isUnlocked = hasCompletedBefore9Am,
                requiredCriteria = "Activity before 9 AM"
            ),
            AchievementBadge(
                id = "streak_cadet",
                title = "Streak Cadet",
                description = "Maintained momentum for 3 consecutive days.",
                icon = Icons.Filled.Star,
                isUnlocked = highestStreak >= 3,
                requiredCriteria = "3-day streak"
            ),
            AchievementBadge(
                id = "consistency_king",
                title = "Consistency King",
                description = "Achieved a full 7-day uninterrupted active streak.",
                icon = Icons.Filled.EmojiEvents,
                isUnlocked = highestStreak >= 7,
                requiredCriteria = "7-day streak"
            ),
            AchievementBadge(
                id = "fortress_of_habit",
                title = "Fortress of Habit",
                description = "Unstoppable discipline! Reached 14 consecutive active days.",
                icon = Icons.Filled.LocalFireDepartment,
                isUnlocked = highestStreak >= 14,
                requiredCriteria = "14-day streak"
            ),
            AchievementBadge(
                id = "frost_guard",
                title = "Frost Guard",
                description = "Protected your hard-earned streak using a safety net freeze.",
                icon = Icons.Filled.Shield,
                isUnlocked = usedFreeze,
                requiredCriteria = "Use a streak freeze"
            ),
            AchievementBadge(
                id = "scribe",
                title = "Master Scribe",
                description = "Jotted down thoughts and created 3 or more notes.",
                icon = Icons.Outlined.Book,
                isUnlocked = noteLogsCount >= 3,
                requiredCriteria = "Log 3 notes"
            )
        )
    }
}
