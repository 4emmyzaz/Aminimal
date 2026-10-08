package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AchievementBadge
import com.example.data.ActivityLog
import com.example.data.ActivityType
import com.example.data.AppDatabase
import com.example.data.AppThemeConfig
import com.example.data.BadgesRepository
import com.example.data.NoteEntity
import com.example.data.Priority
import com.example.data.StreakManager
import com.example.data.TaskEntity
import com.example.data.TaskNoteRepository
import com.example.data.UserPreferencesRepository
import com.example.data.UserStreakState
import com.example.notification.NotificationHelper
import com.example.data.HabitFrequency
import com.example.data.HabitItem
import com.example.data.HabitRepository
import com.example.data.HeatmapWeek
import com.example.util.DataExportHelper
import com.example.util.HapticHelper
import com.example.worker.DailyStreakReminderWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class MainTab {
    NOTES,
    HABITS,
    TASKS
}

enum class TaskFilter {
    ALL,
    PENDING,
    DEADLINES,
    COMPLETED
}

class TaskNoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskNoteRepository
    val streakManager: StreakManager
    private val preferencesRepository = UserPreferencesRepository(application)
    private val badgesRepository = BadgesRepository()
    val habitRepository = HabitRepository(application)

    val streakState: StateFlow<UserStreakState>
    val isTodayActive: StateFlow<Boolean>
    val recentActivityLogs: StateFlow<List<ActivityLog>>

    val themeConfig: StateFlow<AppThemeConfig>
    val streakReminderEnabled: StateFlow<Boolean>
    val customTags: StateFlow<List<String>>

    val fontSize: StateFlow<String>
    val sortingPreference: StateFlow<String>
    val confettiDisabled: StateFlow<Boolean>
    val stickyNotifications: StateFlow<Boolean>
    val firstDayOfWeek: StateFlow<String>
    val iconBadgesEnabled: StateFlow<Boolean>

    val habits: StateFlow<List<HabitItem>> = habitRepository.habitsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _confettiTrigger = MutableStateFlow(0L)
    val confettiTrigger: StateFlow<Long> = _confettiTrigger.asStateFlow()

    private val _selectedNoteTag = MutableStateFlow<String?>(null)
    val selectedNoteTag: StateFlow<String?> = _selectedNoteTag.asStateFlow()

    var lastDeletedTask: TaskEntity? = null
        private set
    var lastDeletedNote: NoteEntity? = null
        private set

    init {
        val db = AppDatabase.getDatabase(application)
        repository = TaskNoteRepository(db.taskDao(), db.noteDao())
        streakManager = StreakManager(db.streakDao(), db.activityLogDao(), application)
        NotificationHelper.createNotificationChannel(application)

        streakState = streakManager.streakStateFlow
            .map { it ?: UserStreakState() }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                UserStreakState()
            )

        isTodayActive = streakState.map { state ->
            state.lastActiveDate == LocalDate.now().toString()
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false
        )

        recentActivityLogs = db.activityLogDao().getRecentLogs(limit = 100)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

        themeConfig = preferencesRepository.themeConfigFlow
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                AppThemeConfig.SYSTEM_DEFAULT
            )

        streakReminderEnabled = preferencesRepository.streakReminderEnabledFlow
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                true
            )

        customTags = preferencesRepository.customTagsFlow
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                listOf("General", "Ideas", "Work", "Personal", "Projects", "Review")
            )

        fontSize = preferencesRepository.fontSizeFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Medium")

        sortingPreference = preferencesRepository.sortingPreferenceFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "By creation date")

        confettiDisabled = preferencesRepository.confettiDisabledFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

        stickyNotifications = preferencesRepository.stickyNotificationsFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

        firstDayOfWeek = preferencesRepository.firstDayOfWeekFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Sunday")

        iconBadgesEnabled = preferencesRepository.iconBadgesEnabledFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

        // Evaluate daily streak on application startup and schedule streak guard
        viewModelScope.launch {
            streakManager.evaluateDailyStreak()
            DailyStreakReminderWorker.schedule(application)
        }

        // Observe tasks and icon badges preference to update notification badges
        viewModelScope.launch {
            combine(
                repository.allTasks,
                iconBadgesEnabled
            ) { tasks, enabled ->
                val pending = tasks.count { !it.isCompleted }
                NotificationHelper.updatePendingBadge(getApplication(), pending, enabled)
            }.collect {}
        }
    }

    val heatmapWeeks: StateFlow<List<HeatmapWeek>> = combine(
        recentActivityLogs,
        habits
    ) { logs, _ ->
        habitRepository.generateHeatmapData(logs, weeksCount = 16)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overallConsistencyScore: StateFlow<Int> = streakState.map { state ->
        when {
            state.currentStreak >= 14 -> 98
            state.currentStreak >= 7 -> 93
            state.currentStreak >= 3 -> 88
            state.currentStreak >= 1 -> 82
            else -> 76
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 88)

    val dailyCompletionPercentage: StateFlow<Int> = combine(
        repository.allTasks,
        habits
    ) { tasks, habitList ->
        val totalCount = tasks.size + habitList.size
        if (totalCount == 0) return@combine 100
        val completedTasks = tasks.count { it.isCompleted }
        val completedHabits = habitList.count { it.isCompletedToday }
        ((completedTasks + completedHabits).toFloat() / totalCount.toFloat() * 100).toInt().coerceIn(0, 100)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 75)

    val badges: StateFlow<List<AchievementBadge>> = combine(
        streakState,
        recentActivityLogs
    ) { state, logs ->
        badgesRepository.getBadges(state, logs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTab = MutableStateFlow(MainTab.HABITS)
    val selectedTab: StateFlow<MainTab> = _selectedTab.asStateFlow()

    private val _taskFilter = MutableStateFlow(TaskFilter.ALL)
    val taskFilter: StateFlow<TaskFilter> = _taskFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val rawTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawNotes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTasks: StateFlow<List<TaskEntity>> = combine(
        rawTasks,
        _taskFilter,
        _searchQuery
    ) { tasks, filter, query ->
        tasks.filter { task ->
            val matchesQuery = query.isBlank() ||
                    task.title.contains(query, ignoreCase = true) ||
                    task.description.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                TaskFilter.ALL -> true
                TaskFilter.PENDING -> !task.isCompleted
                TaskFilter.DEADLINES -> !task.isCompleted && task.dueDateMillis != null
                TaskFilter.COMPLETED -> task.isCompleted
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredNotes: StateFlow<List<NoteEntity>> = combine(
        rawNotes,
        _searchQuery,
        _selectedNoteTag
    ) { notes, query, tagFilter ->
        notes.filter { note ->
            val matchesQuery = query.isBlank() ||
                    note.title.contains(query, ignoreCase = true) ||
                    note.content.contains(query, ignoreCase = true) ||
                    note.tag.contains(query, ignoreCase = true)

            val matchesTag = tagFilter == null || note.tag.equals(tagFilter, ignoreCase = true)

            matchesQuery && matchesTag
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedNoteTag(tag: String?) {
        _selectedNoteTag.value = tag
    }

    fun addCustomTag(tag: String) {
        viewModelScope.launch {
            preferencesRepository.addCustomTag(tag)
        }
    }

    fun removeCustomTag(tag: String) {
        viewModelScope.launch {
            preferencesRepository.removeCustomTag(tag)
            if (_selectedNoteTag.value.equals(tag, ignoreCase = true)) {
                _selectedNoteTag.value = null
            }
        }
    }

    fun setSelectedTab(tab: MainTab) {
        _selectedTab.value = tab
    }

    fun setTaskFilter(filter: TaskFilter) {
        _taskFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setThemeConfig(config: AppThemeConfig) {
        viewModelScope.launch {
            preferencesRepository.setThemeConfig(config)
        }
    }

    fun setStreakReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setStreakReminderEnabled(enabled)
            if (enabled) {
                DailyStreakReminderWorker.schedule(getApplication())
            } else {
                DailyStreakReminderWorker.cancel(getApplication())
            }
        }
    }

    // Task Actions
    fun addTask(
        title: String,
        description: String,
        dueDateMillis: Long?,
        priority: Priority,
        hasReminder: Boolean
    ) {
        viewModelScope.launch {
            val task = TaskEntity(
                title = title.trim(),
                description = description.trim(),
                dueDateMillis = dueDateMillis,
                priority = priority,
                hasReminder = hasReminder,
                createdAt = System.currentTimeMillis()
            )
            val generatedId = repository.insertTask(task)

            if (hasReminder && dueDateMillis != null && dueDateMillis > System.currentTimeMillis()) {
                NotificationHelper.scheduleDeadlineReminder(
                    context = getApplication(),
                    taskId = generatedId,
                    taskTitle = task.title,
                    deadlineMillis = dueDateMillis
                )
            }
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)

            if (task.hasReminder && task.dueDateMillis != null && task.dueDateMillis > System.currentTimeMillis()) {
                NotificationHelper.scheduleDeadlineReminder(
                    context = getApplication(),
                    taskId = task.id,
                    taskTitle = task.title,
                    deadlineMillis = task.dueDateMillis
                )
            } else {
                NotificationHelper.cancelDeadlineReminder(getApplication(), task.id)
            }
        }
    }

    fun updateTask(
        task: TaskEntity,
        newTitle: String,
        newDescription: String,
        newDueDateMillis: Long?,
        newPriority: Priority,
        newHasReminder: Boolean
    ) {
        viewModelScope.launch {
            val updated = task.copy(
                title = newTitle.trim(),
                description = newDescription.trim(),
                dueDateMillis = newDueDateMillis,
                priority = newPriority,
                hasReminder = newHasReminder
            )
            repository.updateTask(updated)

            if (newHasReminder && newDueDateMillis != null && newDueDateMillis > System.currentTimeMillis()) {
                NotificationHelper.scheduleDeadlineReminder(
                    context = getApplication(),
                    taskId = task.id,
                    taskTitle = updated.title,
                    deadlineMillis = newDueDateMillis
                )
            } else {
                NotificationHelper.cancelDeadlineReminder(getApplication(), task.id)
            }
        }
    }

    fun toggleTaskCompleted(task: TaskEntity) {
        viewModelScope.launch {
            val newStatus = !task.isCompleted
            repository.setTaskCompleted(task.id, newStatus)
            if (newStatus) {
                NotificationHelper.cancelDeadlineReminder(getApplication(), task.id)
                HapticHelper.performTaskCompletionFeedback(getApplication())
                val result = streakManager.logActivity(ActivityType.HABIT, task.title)
                if (result.wasStreakIncremented) {
                    HapticHelper.performStreakIncrementFeedback(getApplication())
                }
                _confettiTrigger.value = System.currentTimeMillis()
            } else {
                if (task.hasReminder && task.dueDateMillis != null && task.dueDateMillis > System.currentTimeMillis()) {
                    NotificationHelper.scheduleDeadlineReminder(
                        context = getApplication(),
                        taskId = task.id,
                        taskTitle = task.title,
                        deadlineMillis = task.dueDateMillis
                    )
                }
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        lastDeletedTask = task
        viewModelScope.launch {
            NotificationHelper.cancelDeadlineReminder(getApplication(), task.id)
            repository.deleteTask(task)
            NotificationHelper.showDeletionNotification(
                context = getApplication(),
                itemType = "Task",
                itemTitle = task.title
            )
        }
    }

    fun restoreLastDeletedTask() {
        val task = lastDeletedTask ?: return
        viewModelScope.launch {
            repository.insertTask(task)
            if (task.hasReminder && task.dueDateMillis != null && task.dueDateMillis > System.currentTimeMillis()) {
                NotificationHelper.scheduleDeadlineReminder(
                    context = getApplication(),
                    taskId = task.id,
                    taskTitle = task.title,
                    deadlineMillis = task.dueDateMillis
                )
            }
            lastDeletedTask = null
        }
    }

    // Note Actions
    fun addNote(
        title: String,
        content: String,
        tag: String = "General",
        isPinned: Boolean = false
    ) {
        viewModelScope.launch {
            val note = NoteEntity(
                title = title.trim(),
                content = content.trim(),
                tag = tag.trim().ifEmpty { "General" },
                isPinned = isPinned,
                updatedAt = System.currentTimeMillis()
            )
            repository.insertNote(note)
        }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteNote(note: NoteEntity) {
        lastDeletedNote = note
        viewModelScope.launch {
            repository.deleteNote(note)
            NotificationHelper.showDeletionNotification(
                context = getApplication(),
                itemType = "Note",
                itemTitle = note.title
            )
        }
    }

    fun restoreLastDeletedNote() {
        val note = lastDeletedNote ?: return
        viewModelScope.launch {
            repository.insertNote(note)
            lastDeletedNote = null
        }
    }

    fun toggleNotePin(note: NoteEntity) {
        viewModelScope.launch {
            repository.toggleNotePin(note.id, !note.isPinned)
        }
    }

    // Habit Stacking & Advanced Habit Engine Actions
    fun toggleHabitStep(habitId: String, stepId: String) {
        HapticHelper.performTickFeedback(getApplication())
        habitRepository.toggleStep(habitId, stepId) {
            // Callback when entire habit stack is completed
            HapticHelper.performStreakIncrementFeedback(getApplication())
            viewModelScope.launch {
                streakManager.logActivity(ActivityType.HABIT, "Completed Habit Stack")
                _confettiTrigger.value = System.currentTimeMillis()
            }
        }
    }

    fun updateHabitCount(habitId: String, delta: Int) {
        HapticHelper.performTickFeedback(getApplication())
        habitRepository.updateCount(habitId, delta) {
            // Callback when habit target is reached
            HapticHelper.performStreakIncrementFeedback(getApplication())
            viewModelScope.launch {
                streakManager.logActivity(ActivityType.HABIT, "Reached Habit Target")
                _confettiTrigger.value = System.currentTimeMillis()
            }
        }
    }

    fun updateHabitStepCount(habitId: String, stepId: String, delta: Int) {
        HapticHelper.performTickFeedback(getApplication())
        habitRepository.updateStepCount(habitId, stepId, delta) {
            HapticHelper.performStreakIncrementFeedback(getApplication())
            viewModelScope.launch {
                streakManager.logActivity(ActivityType.HABIT, "Reached Habit Step Target")
                _confettiTrigger.value = System.currentTimeMillis()
            }
        }
    }

    fun completeHabitDirectly(habitId: String) {
        HapticHelper.performTaskCompletionFeedback(getApplication())
        habitRepository.completeHabitDirectly(habitId) {
            HapticHelper.performStreakIncrementFeedback(getApplication())
            viewModelScope.launch {
                streakManager.logActivity(ActivityType.HABIT, "Completed Daily Habit")
                _confettiTrigger.value = System.currentTimeMillis()
            }
        }
    }

    fun addHabitStack(
        title: String,
        anchorAction: String,
        steps: List<String>,
        frequency: HabitFrequency = HabitFrequency.DAILY
    ) {
        habitRepository.addHabitStack(
            title = title,
            anchorAction = anchorAction,
            steps = steps,
            frequency = frequency
        )
    }

    fun addHabitItem(habit: HabitItem) {
        habitRepository.addHabitItem(habit)
    }

    fun updateHabit(habit: HabitItem) {
        habitRepository.updateHabit(habit)
    }

    fun deleteHabit(habitId: String) {
        habitRepository.deleteHabit(habitId)
    }

    fun duplicateHabit(habitId: String) {
        habitRepository.duplicateHabit(habitId)
    }

    fun togglePauseHabit(habitId: String) {
        habitRepository.togglePauseHabit(habitId)
    }

    fun resetHabitTodayProgress(habitId: String) {
        habitRepository.resetHabitTodayProgress(habitId)
    }

    fun reorderHabitSteps(habitId: String, fromIndex: Int, toIndex: Int) {
        habitRepository.reorderHabitSteps(habitId, fromIndex, toIndex)
    }

    // Data Export
    fun exportDataJson(): String {
        return DataExportHelper.generateBackupJson(
            streakState = streakState.value,
            tasks = rawTasks.value,
            notes = rawNotes.value,
            habits = habits.value,
            activityLogs = recentActivityLogs.value
        )
    }

    fun exportDataCsv(): String {
        return DataExportHelper.generateBackupCsv(
            tasks = rawTasks.value,
            notes = rawNotes.value,
            habits = habits.value
        )
    }

    fun importDataJson(jsonString: String) {
        viewModelScope.launch {
            DataExportHelper.parseAndRestoreBackup(getApplication(), jsonString) {
                // Restored successfully
            }
        }
    }

    fun setFontSize(size: String) { viewModelScope.launch { preferencesRepository.setFontSize(size) } }
    fun setSortingPreference(sort: String) { viewModelScope.launch { preferencesRepository.setSortingPreference(sort) } }
    fun setConfettiDisabled(disabled: Boolean) { viewModelScope.launch { preferencesRepository.setConfettiDisabled(disabled) } }
    fun setStickyNotifications(sticky: Boolean) { viewModelScope.launch { preferencesRepository.setStickyNotifications(sticky) } }
    fun setFirstDayOfWeek(day: String) { viewModelScope.launch { preferencesRepository.setFirstDayOfWeek(day) } }
    fun setIconBadgesEnabled(enabled: Boolean) { viewModelScope.launch { preferencesRepository.setIconBadgesEnabled(enabled) } }

    // Streak & Safety Net Actions
    fun purchaseStreakFreeze() {
        viewModelScope.launch {
            streakManager.purchaseFreezeWithCoins()
        }
    }

    fun checkHabitDirectly(habitName: String = "Daily Focus Habit") {
        viewModelScope.launch {
            streakManager.logActivity(ActivityType.HABIT, habitName)
            _confettiTrigger.value = System.currentTimeMillis()
        }
    }
}
