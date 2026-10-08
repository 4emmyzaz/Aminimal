package com.example.data

import java.time.LocalDate

/**
 * Trigger classification based on behavioral anchoring psychology (Atomic Habits).
 */
enum class TriggerType(val label: String, val promptPrefix: String) {
    TIME("Time", "At"),
    EVENT("Event", "After"),
    LOCATION("Location", "When at"),
    ENVIRONMENT("Object / Environment", "When"),
    COMPLETION("Task Completion", "After completing"),
    MANUAL("Manual", "When I choose to start")
}

/**
 * Step interaction types supported within Habit Stacks.
 */
enum class HabitStepType {
    BOOLEAN,       // Binary checkbox
    QUANTITATIVE,  // Target count + Unit
    DURATION,      // Minutes timer / duration
    COUNT,         // Repetitions / Count
    PAGES          // Book / reading pages
}

/**
 * Frequency configuration for habit tracking.
 */
enum class HabitFrequency {
    DAILY,
    WEEKLY_X_TIMES,
    COUNT_BASED
}

/**
 * A single sequential step linked inside a Habit Stack.
 */
data class HabitStep(
    val id: String,
    val title: String,
    val durationMinutes: Int? = null,
    val isCompleted: Boolean = false,
    val type: HabitStepType = HabitStepType.BOOLEAN,
    val targetValue: Int = 1,
    val currentValue: Int = 0,
    val unit: String = ""
) {
    val isStepCompleted: Boolean
        get() = when (type) {
            HabitStepType.BOOLEAN -> isCompleted
            else -> currentValue >= targetValue || isCompleted
        }

    val progress: Float
        get() = when (type) {
            HabitStepType.BOOLEAN -> if (isCompleted) 1f else 0f
            else -> if (targetValue <= 0) (if (isCompleted) 1f else 0f) else (currentValue.toFloat() / targetValue.toFloat()).coerceIn(0f, 1f)
        }
}

/**
 * Represents a rich habit with Habit Stacking (anchor action + sequential steps),
 * flexible frequency/target configurations, and the "Never Miss Twice" streak safeguard.
 */
data class HabitItem(
    val id: String,
    val title: String,
    val anchorAction: String = "", // e.g., "After morning coffee", "After shutting work laptop"
    val triggerType: TriggerType = TriggerType.EVENT,
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val targetWeeklyDays: Int = 7,
    val targetCount: Int = 1,
    val currentCount: Int = 0,
    val unit: String = "", // e.g. "ml", "pages", "mins", "reps"
    val sequentialSteps: List<HabitStep> = emptyList(),
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastCompletedDate: String? = null,
    val isGraceWindowActive: Boolean = false,
    val isCompletedToday: Boolean = false,
    val isPaused: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val emoji: String = "💪",
    val colorHex: String = "#3B82F6",
    val category: String = "Productivity",
    val customCategory: String = "",
    val completedDates: List<String> = emptyList(),
    val reminderTime: String = "",
    val note: String = ""
) {
    val totalEstimatedMinutes: Int
        get() = sequentialSteps.mapNotNull { it.durationMinutes }.sum()

    val completedStepsCount: Int
        get() = sequentialSteps.count { it.isStepCompleted }

    val completionPercentage: Float
        get() = when {
            sequentialSteps.isNotEmpty() -> {
                val totalProgress = sequentialSteps.sumOf { it.progress.toDouble() }.toFloat()
                (totalProgress / sequentialSteps.size.toFloat()).coerceIn(0f, 1f)
            }
            frequency == HabitFrequency.COUNT_BASED -> {
                if (targetCount <= 0) 0f else (currentCount.toFloat() / targetCount.toFloat()).coerceIn(0f, 1f)
            }
            else -> {
                if (isCompletedToday) 1f else 0f
            }
        }

    val isEntirelyCompleted: Boolean
        get() = when {
            sequentialSteps.isNotEmpty() -> sequentialSteps.all { it.isStepCompleted }
            frequency == HabitFrequency.COUNT_BASED -> currentCount >= targetCount
            else -> isCompletedToday
        }
}

/**
 * A data point in the Habit Completion Heatmap grid.
 */
data class HeatmapDay(
    val date: LocalDate,
    val dateString: String,
    val count: Int,
    val level: Int // 0 (empty) to 4 (intense)
)

/**
 * A weekly column containing up to 7 days for grid/heatmap display.
 */
data class HeatmapWeek(
    val weekIndex: Int,
    val days: List<HeatmapDay>
)
