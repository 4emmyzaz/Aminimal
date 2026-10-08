package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.ActivityType
import com.example.data.AppDatabase
import com.example.data.StreakManager
import com.example.data.UserStreakState
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StreakManagerTest {

    private lateinit var database: AppDatabase
    private lateinit var streakManager: StreakManager

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        streakManager = StreakManager(database.streakDao(), database.activityLogDao(), context)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInitialStreakStateDefaults() = runBlocking {
        val state = streakManager.getOrCreateStreakState()
        assertEquals(0, state.currentStreak)
        assertEquals(UserStreakState.DEFAULT_INITIAL_FREEZES, state.streakFreezesCount)
        assertEquals(2, state.streakFreezesCount)
    }

    @Test
    fun testFirstActivityIgnitesStreak() = runBlocking {
        val today = LocalDate.of(2025, 1, 1)
        val result = streakManager.logActivity(ActivityType.HABIT, "Morning Meditation", date = today)

        assertEquals(1, result.currentStreak)
        assertTrue(result.wasStreakIncremented)
        assertEquals(2, result.freezesRemaining)

        val state = streakManager.getOrCreateStreakState()
        assertEquals(1, state.currentStreak)
        assertEquals("2025-01-01", state.lastActiveDate)
    }

    @Test
    fun testMultipleActivitiesSameDayCountOnce() = runBlocking {
        val today = LocalDate.of(2025, 1, 1)
        // First activity (Habit)
        streakManager.logActivity(ActivityType.HABIT, "Habit 1", date = today)
        // Second activity (Note)
        val secondResult = streakManager.logActivity(ActivityType.NOTE, "Note 1", date = today)

        assertEquals(1, secondResult.currentStreak)
        assertFalse(secondResult.wasStreakIncremented)
    }

    @Test
    fun testConsecutiveDayIncrementsStreak() = runBlocking {
        val day1 = LocalDate.of(2025, 1, 1)
        val day2 = LocalDate.of(2025, 1, 2)

        streakManager.logActivity(ActivityType.HABIT, "Habit Day 1", date = day1)
        val result = streakManager.logActivity(ActivityType.NOTE, "Note Day 2", date = day2)

        assertEquals(2, result.currentStreak)
        assertTrue(result.wasStreakIncremented)
    }

    @Test
    fun testMissingOneDayConsumesOneFreezeAndPreservesStreak() = runBlocking {
        val day1 = LocalDate.of(2025, 1, 1) // Day 1 Active
        // Day 2 (2025-01-02) Missed
        val day3 = LocalDate.of(2025, 1, 3) // Day 3 Active

        streakManager.logActivity(ActivityType.HABIT, "Habit Day 1", date = day1)
        val result = streakManager.logActivity(ActivityType.HABIT, "Habit Day 3", date = day3)

        // Streak should be preserved and incremented: 1 + 1 = 2
        assertEquals(2, result.currentStreak)
        assertEquals(1, result.freezesConsumed)
        assertEquals(1, result.freezesRemaining) // 2 - 1 = 1 freeze left
    }

    @Test
    fun testMissingTwoDaysConsumesTwoFreezes() = runBlocking {
        val day1 = LocalDate.of(2025, 1, 1) // Active
        // Day 2 & Day 3 Missed (2 missed days)
        val day4 = LocalDate.of(2025, 1, 4) // Active

        streakManager.logActivity(ActivityType.HABIT, "Habit Day 1", date = day1)
        val result = streakManager.logActivity(ActivityType.NOTE, "Note Day 4", date = day4)

        assertEquals(2, result.currentStreak)
        assertEquals(2, result.freezesConsumed)
        assertEquals(0, result.freezesRemaining) // Both freezes used
    }

    @Test
    fun testGapExceedingAvailableFreezesResetsStreak() = runBlocking {
        val day1 = LocalDate.of(2025, 1, 1) // Active
        // Miss 3 days (Jan 2, Jan 3, Jan 4) with only 2 freezes available
        val day5 = LocalDate.of(2025, 1, 5) // Active

        streakManager.logActivity(ActivityType.HABIT, "Habit Day 1", date = day1)
        val result = streakManager.logActivity(ActivityType.HABIT, "Habit Day 5", date = day5)

        // Gap is 3 days > 2 freezes -> resets, but since user was active on day 5, starts at 1
        assertTrue(result.wasStreakReset)
        assertEquals(1, result.currentStreak)
        assertEquals(0, result.freezesRemaining)
    }

    @Test
    fun testMilestoneReplenishesFreeze() = runBlocking {
        // First consume 1 freeze
        val day1 = LocalDate.of(2025, 1, 1)
        val day3 = LocalDate.of(2025, 1, 3) // Uses 1 freeze, streak becomes 2, freezes becomes 1
        streakManager.logActivity(ActivityType.HABIT, "Habit", date = day1)
        streakManager.logActivity(ActivityType.HABIT, "Habit", date = day3)

        // Next day reaches 3-day milestone!
        val day4 = LocalDate.of(2025, 1, 4)
        val result = streakManager.logActivity(ActivityType.HABIT, "Habit", date = day4)

        assertEquals(3, result.currentStreak)
        assertEquals(3, result.milestoneRewardEarned)
        // Freeze was replenished from 1 back to 2!
        assertEquals(2, result.freezesRemaining)
    }

    @Test
    fun testPostResetTwoDayStreakRestoresFreezes() = runBlocking {
        // Trigger a reset
        val day1 = LocalDate.of(2025, 1, 1)
        val day5 = LocalDate.of(2025, 1, 5) // Missed 3 days -> streak resets to 1, freezes = 0
        streakManager.logActivity(ActivityType.HABIT, "Habit", date = day1)
        streakManager.logActivity(ActivityType.HABIT, "Habit", date = day5)

        // Day 2 post-reset: consecutive day
        val day6 = LocalDate.of(2025, 1, 6)
        val result = streakManager.logActivity(ActivityType.HABIT, "Habit", date = day6)

        assertEquals(2, result.currentStreak)
        assertTrue(result.postResetRecoveryEarned)
        assertEquals(2, result.freezesRemaining) // Fully recovered to 2 freezes!
    }

    @Test
    fun testPurchaseFreezeWithCoins() = runBlocking {
        val state = streakManager.getOrCreateStreakState()
        // Reduce freezes to 1
        database.streakDao().insertOrUpdate(state.copy(streakFreezesCount = 1, coins = 100))

        val success = streakManager.purchaseFreezeWithCoins()
        assertTrue(success)

        val updated = streakManager.getOrCreateStreakState()
        assertEquals(2, updated.streakFreezesCount)
        assertEquals(50, updated.coins) // 100 - 50 = 50
    }
}
