package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

enum class AppThemeConfig {
    SYSTEM_DEFAULT,
    LIGHT,
    DARK
}

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_CONFIG = stringPreferencesKey("theme_config")
        val STREAK_REMINDER_ENABLED = booleanPreferencesKey("streak_reminder_enabled")
        val CUSTOM_TAGS = stringPreferencesKey("custom_tags")
        val REMINDER_HOUR = stringPreferencesKey("reminder_hour")
        val FONT_SIZE = stringPreferencesKey("font_size")
        val SORTING_PREFERENCE = stringPreferencesKey("sorting_preference")
        val CONFETTI_DISABLED = booleanPreferencesKey("confetti_disabled")
        val STICKY_NOTIFICATIONS = booleanPreferencesKey("sticky_notifications")
        val FIRST_DAY_OF_WEEK = stringPreferencesKey("first_day_of_week")
        val ICON_BADGES_ENABLED = booleanPreferencesKey("icon_badges_enabled")
    }

    private val defaultTags = setOf("General", "Ideas", "Work", "Personal", "Projects", "Review")

    val fontSizeFlow: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.FONT_SIZE] ?: "Medium" }

    val sortingPreferenceFlow: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.SORTING_PREFERENCE] ?: "By creation date" }

    val confettiDisabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.CONFETTI_DISABLED] ?: false }

    val stickyNotificationsFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.STICKY_NOTIFICATIONS] ?: false }

    val firstDayOfWeekFlow: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.FIRST_DAY_OF_WEEK] ?: "Sunday" }

    val iconBadgesEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.ICON_BADGES_ENABLED] ?: true }

    suspend fun setFontSize(size: String) {
        context.dataStore.edit { it[PreferencesKeys.FONT_SIZE] = size }
    }

    suspend fun setSortingPreference(sort: String) {
        context.dataStore.edit { it[PreferencesKeys.SORTING_PREFERENCE] = sort }
    }

    suspend fun setConfettiDisabled(disabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.CONFETTI_DISABLED] = disabled }
    }

    suspend fun setStickyNotifications(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.STICKY_NOTIFICATIONS] = enabled }
    }

    suspend fun setFirstDayOfWeek(day: String) {
        context.dataStore.edit { it[PreferencesKeys.FIRST_DAY_OF_WEEK] = day }
    }

    suspend fun setIconBadgesEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ICON_BADGES_ENABLED] = enabled }
    }

    val customTagsFlow: Flow<List<String>> = context.dataStore.data
        .map { preferences ->
            val stored = preferences[PreferencesKeys.CUSTOM_TAGS]
            if (stored.isNullOrBlank()) {
                defaultTags.toList()
            } else {
                val parsed = stored.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                (defaultTags + parsed).distinct()
            }
        }

    suspend fun addCustomTag(newTag: String) {
        val clean = newTag.trim()
        if (clean.isBlank()) return
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.CUSTOM_TAGS] ?: ""
            val tags = current.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()
            tags.add(clean)
            preferences[PreferencesKeys.CUSTOM_TAGS] = tags.joinToString(",")
        }
    }

    suspend fun removeCustomTag(tagToRemove: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.CUSTOM_TAGS] ?: ""
            val tags = current.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()
            tags.remove(tagToRemove.trim())
            preferences[PreferencesKeys.CUSTOM_TAGS] = tags.joinToString(",")
        }
    }

    val themeConfigFlow: Flow<AppThemeConfig> = context.dataStore.data
        .map { preferences ->
            val themeString = preferences[PreferencesKeys.THEME_CONFIG]
                ?: AppThemeConfig.SYSTEM_DEFAULT.name
            try {
                AppThemeConfig.valueOf(themeString)
            } catch (e: Exception) {
                AppThemeConfig.SYSTEM_DEFAULT
            }
        }

    val streakReminderEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.STREAK_REMINDER_ENABLED] ?: true
        }

    suspend fun setThemeConfig(themeConfig: AppThemeConfig) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_CONFIG] = themeConfig.name
        }
    }

    suspend fun setStreakReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.STREAK_REMINDER_ENABLED] = enabled
        }
    }
}
