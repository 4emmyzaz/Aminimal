package com.example.data

import android.content.Context
import com.example.widget.StreakGlanceWidget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Result data class returned by evaluateDailyStreak, providing details about what changed.
 */
data class StreakEvaluationResult(
    val currentStreak: Int,
    val freezesRemaining: Int,
    val freezesConsumed: Int = 0,
    val wasStreakReset: Boolean = false,
    val wasStreakIncremented: Boolean = false,
    val milestoneRewardEarned: Int? = null,
    val postResetRecoveryEarned: Boolean = false,
    val message: String = ""
)

/**
 * Manages streak calculations, safety net freeze consumption, activity logging,
 * and freeze replenishments according to the Elevate streak model.
 */
class StreakManager(
    private val streakDao: StreakDao,
    private val activityLogDao: ActivityLogDao,
    private val context: Context? = null
) {
    val streakStateFlow: Flow<UserStreakState?> = streakDao.getStreakStateFlow()
    val recentLogsFlow: Flow<List<ActivityLog>> = activityLogDao.getRecentLogs()

    /**
     * Ensures an initialized UserStreakState exists in the database.
     * New users start with 0 streak and the default 2 safety net freezes.
     */
    suspend fun getOrCreateStreakState(): UserStreakState {
        val existing = streakDao.getStreakState()
        if (existing != null) return existing

        val initial = UserStreakState(
            id = 1,
            currentStreak = 0,
            streakFreezesCount = UserStreakState.DEFAULT_INITIAL_FREEZES,
            lastActiveDate = null,
            freezeLastUsedDate = null,
            postResetStreakDays = 0,
            coins = 100,
            highestStreak = 0
        )
        streakDao.insertOrUpdate(initial)
        return initial
    }

    /**
     * Evaluates daily streak progress and gap days against the current state.
     *
     * @param todayDate The current date being evaluated (defaults to LocalDate.now()).
     * @param hasActivityToday Explicit indicator if an activity occurred today. If null, queries DB.
     */
    suspend fun evaluateDailyStreak(
        todayDate: LocalDate = LocalDate.now(),
        hasActivityToday: Boolean? = null
    ): StreakEvaluationResult {
        val currentState = getOrCreateStreakState()
        val todayStr = todayDate.toString()

        val isTodayActive = hasActivityToday ?: (activityLogDao.countActivitiesOnDate(todayStr) > 0)

        // Case 1: First-time user with no prior active date
        if (currentState.lastActiveDate == null) {
            if (isTodayActive) {
                val newState = currentState.copy(
                    currentStreak = 1,
                    lastActiveDate = todayStr,
                    highestStreak = maxOf(currentState.highestStreak, 1),
                    postResetStreakDays = 0
                )
                streakDao.insertOrUpdate(newState)
                notifyWidgetUpdate()
                return StreakEvaluationResult(
                    currentStreak = 1,
                    freezesRemaining = newState.streakFreezesCount,
                    wasStreakIncremented = true,
                    message = "Streak started! 1 day active."
                )
            } else {
                return StreakEvaluationResult(
                    currentStreak = 0,
                    freezesRemaining = currentState.streakFreezesCount,
                    message = "Complete a habit or write a note today to ignite your streak!"
                )
            }
        }

        val lastActiveLocalDate = try {
            LocalDate.parse(currentState.lastActiveDate)
        } catch (e: Exception) {
            todayDate
        }

        val daysDifference = ChronoUnit.DAYS.between(lastActiveLocalDate, todayDate)

        // Negative difference safeguard (e.g., device clock adjusted backwards)
        if (daysDifference < 0) {
            return StreakEvaluationResult(
                currentStreak = currentState.currentStreak,
                freezesRemaining = currentState.streakFreezesCount,
                message = "Device time out of sync."
            )
        }

        // Case 2: Activity completed on the same calendar day (idempotent)
        if (daysDifference == 0L) {
            // Already counted today. Multiple completions on the same day count as 1 active day.
            return StreakEvaluationResult(
                currentStreak = currentState.currentStreak,
                freezesRemaining = currentState.streakFreezesCount,
                message = "Already active today! Streak is secure."
            )
        }

        // Case 3: Exactly 1 day gap (Consecutive calendar day)
        if (daysDifference == 1L) {
            if (isTodayActive) {
                val newStreak = currentState.currentStreak + 1
                var newFreezes = currentState.streakFreezesCount
                var milestoneReward: Int? = null
                var postResetRecovery = false

                // Track post-reset consecutive days for safety net recovery
                val newPostResetDays = if (currentState.postResetStreakDays > 0 || currentState.currentStreak == 0) {
                    currentState.postResetStreakDays + 1
                } else {
                    0
                }

                // Replenishment 1: Milestone reward (3, 7, 14, 30 days)
                val milestones = listOf(3, 7, 14, 30)
                if (newStreak in milestones && newFreezes < UserStreakState.MAX_FREEZES) {
                    newFreezes = minOf(UserStreakState.MAX_FREEZES, newFreezes + 1)
                    milestoneReward = newStreak
                }

                // Replenishment 2: 2-day recovery post-reset restores to 2 freezes
                if (newPostResetDays >= 2 && newFreezes < UserStreakState.MAX_FREEZES) {
                    newFreezes = UserStreakState.MAX_FREEZES
                    postResetRecovery = true
                }

                val newState = currentState.copy(
                    currentStreak = newStreak,
                    streakFreezesCount = newFreezes,
                    lastActiveDate = todayStr,
                    highestStreak = maxOf(currentState.highestStreak, newStreak),
                    postResetStreakDays = if (postResetRecovery) 0 else newPostResetDays
                )
                streakDao.insertOrUpdate(newState)
                notifyWidgetUpdate()

                return StreakEvaluationResult(
                    currentStreak = newStreak,
                    freezesRemaining = newFreezes,
                    wasStreakIncremented = true,
                    milestoneRewardEarned = milestoneReward,
                    postResetRecoveryEarned = postResetRecovery,
                    message = "Streak extended! $newStreak days in a row."
                )
            } else {
                // Today has not yet registered an activity, but streak is not broken yet
                return StreakEvaluationResult(
                    currentStreak = currentState.currentStreak,
                    freezesRemaining = currentState.streakFreezesCount,
                    message = "Complete a habit or note today to maintain your ${currentState.currentStreak}-day streak!"
                )
            }
        }

        // Case 4: Gap > 1 day (One or more calendar days missed)
        val missedDays = (daysDifference - 1).toInt()
        val availableFreezes = currentState.streakFreezesCount

        if (missedDays <= availableFreezes) {
            // Safety Net: Sufficient freezes to protect the streak across all missed days
            val remainingFreezes = availableFreezes - missedDays
            val lastFreezeDate = todayDate.minusDays(1).toString()

            if (isTodayActive) {
                // User logged an activity today after the frozen days: streak increments!
                val newStreak = currentState.currentStreak + 1
                var finalFreezes = remainingFreezes
                var milestoneReward: Int? = null

                if (newStreak in listOf(3, 7, 14, 30) && finalFreezes < UserStreakState.MAX_FREEZES) {
                    finalFreezes = minOf(UserStreakState.MAX_FREEZES, finalFreezes + 1)
                    milestoneReward = newStreak
                }

                val newState = currentState.copy(
                    currentStreak = newStreak,
                    streakFreezesCount = finalFreezes,
                    lastActiveDate = todayStr,
                    freezeLastUsedDate = lastFreezeDate,
                    highestStreak = maxOf(currentState.highestStreak, newStreak)
                )
                streakDao.insertOrUpdate(newState)
                notifyWidgetUpdate()

                return StreakEvaluationResult(
                    currentStreak = newStreak,
                    freezesRemaining = finalFreezes,
                    freezesConsumed = missedDays,
                    wasStreakIncremented = true,
                    milestoneRewardEarned = milestoneReward,
                    message = "Used $missedDays streak freeze(s). Streak preserved and extended to $newStreak days!"
                )
            } else {
                // Passive check today: freezes consumed to protect streak so far
                val newState = currentState.copy(
                    streakFreezesCount = remainingFreezes,
                    freezeLastUsedDate = lastFreezeDate
                )
                streakDao.insertOrUpdate(newState)
                notifyWidgetUpdate()

                return StreakEvaluationResult(
                    currentStreak = currentState.currentStreak,
                    freezesRemaining = remainingFreezes,
                    freezesConsumed = missedDays,
                    message = "Safety Net saved your streak! $missedDays freeze(s) consumed. Complete an activity today!"
                )
            }
        } else {
            // Freezes exhausted: Missed days exceed safety net capacity. Streak resets to 0.
            if (isTodayActive) {
                // Started a fresh streak today
                val newState = currentState.copy(
                    currentStreak = 1,
                    streakFreezesCount = 0,
                    lastActiveDate = todayStr,
                    freezeLastUsedDate = todayDate.minusDays(1).toString(),
                    postResetStreakDays = 1
                )
                streakDao.insertOrUpdate(newState)
                notifyWidgetUpdate()

                return StreakEvaluationResult(
                    currentStreak = 1,
                    freezesRemaining = 0,
                    freezesConsumed = availableFreezes,
                    wasStreakReset = true,
                    wasStreakIncremented = true,
                    message = "Streak reset after missed days. New streak started today (1 day)!"
                )
            } else {
                // Streak is reset to 0, freezes depleted
                val newState = currentState.copy(
                    currentStreak = 0,
                    streakFreezesCount = 0,
                    postResetStreakDays = 0
                )
                streakDao.insertOrUpdate(newState)
                notifyWidgetUpdate()

                return StreakEvaluationResult(
                    currentStreak = 0,
                    freezesRemaining = 0,
                    freezesConsumed = availableFreezes,
                    wasStreakReset = true,
                    message = "Streak was reset due to $missedDays missed day(s)."
                )
            }
        }
    }

    /**
     * Logs an activity (HABIT or NOTE) and immediately re-evaluates the streak.
     */
    suspend fun logActivity(
        type: ActivityType,
        details: String = "",
        date: LocalDate = LocalDate.now()
    ): StreakEvaluationResult {
        val log = ActivityLog(
            activityType = type,
            date = date.toString(),
            timestamp = System.currentTimeMillis(),
            details = details
        )
        activityLogDao.insertLog(log)
        return evaluateDailyStreak(todayDate = date, hasActivityToday = true)
    }

    /**
     * Replenishes a streak freeze using in-app reward coins.
     */
    suspend fun purchaseFreezeWithCoins(): Boolean {
        val state = getOrCreateStreakState()
        if (state.streakFreezesCount >= UserStreakState.MAX_FREEZES) {
            return false // Already at max capacity
        }
        if (state.coins < UserStreakState.FREEZE_COIN_COST) {
            return false // Insufficient coins
        }

        val newState = state.copy(
            coins = state.coins - UserStreakState.FREEZE_COIN_COST,
            streakFreezesCount = state.streakFreezesCount + 1
        )
        streakDao.insertOrUpdate(newState)
        notifyWidgetUpdate()
        return true
    }

    /**
     * Replenishes freezes directly (e.g. for testing or promotional rewards).
     */
    suspend fun addFreezes(amount: Int): UserStreakState {
        val state = getOrCreateStreakState()
        val newCount = minOf(UserStreakState.MAX_FREEZES, state.streakFreezesCount + amount)
        val newState = state.copy(streakFreezesCount = newCount)
        streakDao.insertOrUpdate(newState)
        notifyWidgetUpdate()
        return newState
    }

    /**
     * Adds reward coins.
     */
    suspend fun addCoins(amount: Int): UserStreakState {
        val state = getOrCreateStreakState()
        val newState = state.copy(coins = state.coins + amount)
        streakDao.insertOrUpdate(newState)
        return newState
    }

    /**
     * Resets streak data for testing or clear data.
     */
    suspend fun resetAll(): UserStreakState {
        val default = UserStreakState(
            id = 1,
            currentStreak = 0,
            streakFreezesCount = UserStreakState.DEFAULT_INITIAL_FREEZES,
            lastActiveDate = null,
            freezeLastUsedDate = null,
            postResetStreakDays = 0,
            coins = 100,
            highestStreak = 0
        )
        streakDao.insertOrUpdate(default)
        activityLogDao.clear()
        notifyWidgetUpdate()
        return default
    }

    private fun notifyWidgetUpdate() {
        context?.let { ctx ->
            try {
                StreakGlanceWidget.triggerUpdate(ctx)
            } catch (e: Throwable) {
                // Silently handle if widget update fails in test or background
            }
        }
    }
}
