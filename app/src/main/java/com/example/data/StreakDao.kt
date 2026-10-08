package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {
    @Query("SELECT * FROM user_streak_state WHERE id = 1 LIMIT 1")
    fun getStreakStateFlow(): Flow<UserStreakState?>

    @Query("SELECT * FROM user_streak_state WHERE id = 1 LIMIT 1")
    suspend fun getStreakState(): UserStreakState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(state: UserStreakState)

    @Query("DELETE FROM user_streak_state")
    suspend fun clear()
}
