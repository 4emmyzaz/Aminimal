package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.HabitItem
import com.example.data.NoteEntity
import com.example.data.TaskEntity
import com.example.ui.components.AboutUsDialog
import com.example.ui.components.AddHabitSheet
import com.example.ui.components.AppDrawerContent
import com.example.ui.components.CalendarViewSheet
import com.example.ui.components.ConfettiOverlay
import com.example.ui.components.DailyLogSheet
import com.example.ui.components.DataSyncBackupSheet
import com.example.ui.components.DrawerDestination
import com.example.ui.components.HabitDetailSheet
import com.example.ui.components.HabitStacksSheet
import com.example.ui.components.HabitTipsSheet
import com.example.ui.components.InterfaceSettingsSheet
import com.example.ui.components.NoteEditorSheet
import com.example.ui.components.NoteItemCard
import com.example.ui.components.NotificationPermissionBanner
import com.example.ui.components.SettingsDialog
import com.example.ui.components.StreakHeader
import com.example.ui.components.TaskEditorSheet
import com.example.ui.components.TaskItemCard
import com.example.ui.theme.AccentMint
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.widget.StreakGlanceWidget
import kotlinx.coroutines.launch

@Composable
fun DashedClockIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.size(24.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(20.dp)) {
            val strokeWidth = 2.dp.toPx()
            val radius = size.minDimension / 2 - strokeWidth
            val center = Offset(size.width / 2, size.height / 2)
            
            // Draw partially dashed circle
            drawCircle(
                color = tint,
                radius = radius,
                center = center,
                style = Stroke(
                    width = strokeWidth,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f), 0f)
                )
            )
            
            // Clock hands
            drawLine(
                color = tint,
                start = center,
                end = Offset(center.x + radius * 0.35f, center.y - radius * 0.35f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = tint,
                start = center,
                end = Offset(center.x, center.y - radius * 0.7f),
                strokeWidth = strokeWidth * 0.8f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun MainScreen(
    quickAction: String? = null,
    viewModel: TaskNoteViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val taskFilter by viewModel.taskFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val rawTasks by viewModel.rawTasks.collectAsStateWithLifecycle()
    val filteredTasks by viewModel.filteredTasks.collectAsStateWithLifecycle()
    val rawNotes by viewModel.rawNotes.collectAsStateWithLifecycle()
    val filteredNotes by viewModel.filteredNotes.collectAsStateWithLifecycle()

    val streakState by viewModel.streakState.collectAsStateWithLifecycle()
    val isTodayActive by viewModel.isTodayActive.collectAsStateWithLifecycle()
    val recentActivityLogs by viewModel.recentActivityLogs.collectAsStateWithLifecycle()
    val themeConfig by viewModel.themeConfig.collectAsStateWithLifecycle()
    val streakReminderEnabled by viewModel.streakReminderEnabled.collectAsStateWithLifecycle()
    val badges by viewModel.badges.collectAsStateWithLifecycle()
    val confettiTrigger by viewModel.confettiTrigger.collectAsStateWithLifecycle()

    val fontSize by viewModel.fontSize.collectAsStateWithLifecycle()
    val sortingPreference by viewModel.sortingPreference.collectAsStateWithLifecycle()
    val confettiDisabled by viewModel.confettiDisabled.collectAsStateWithLifecycle()
    val stickyNotifications by viewModel.stickyNotifications.collectAsStateWithLifecycle()
    val firstDayOfWeek by viewModel.firstDayOfWeek.collectAsStateWithLifecycle()
    val iconBadgesEnabled by viewModel.iconBadgesEnabled.collectAsStateWithLifecycle()

    // Habits & Tags
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val customTags by viewModel.customTags.collectAsStateWithLifecycle()
    val selectedNoteTag by viewModel.selectedNoteTag.collectAsStateWithLifecycle()

    // Navigation Drawer State
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var currentDrawerDestination by remember { mutableStateOf(DrawerDestination.DASHBOARD) }

    var showOptionsMenu by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Sheets
    var showAboutUsDialog by remember { mutableStateOf(false) }
    var showHabitStacksSheet by remember { mutableStateOf(false) }
    var showAddHabitSheet by remember { mutableStateOf(false) }
    var showDailyLogSheet by remember { mutableStateOf(false) }
    var showCalendarSheet by remember { mutableStateOf(false) }
    var showInterfaceSheet by remember { mutableStateOf(false) }
    var showHabitTipsSheet by remember { mutableStateOf(false) }
    var showBackupSheet by remember { mutableStateOf(false) }

    var showTaskEditor by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }

    var showNoteEditor by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<NoteEntity?>(null) }

    var selectedHabitForDetail by remember { mutableStateOf<HabitItem?>(null) }

    // Horizontal Pager State (0: Notes, 1: Habits, 2: Tasks)
    val pagerState = rememberPagerState(
        initialPage = when (selectedTab) {
            MainTab.NOTES -> 0
            MainTab.HABITS -> 1
            MainTab.TASKS -> 2
        },
        pageCount = { 3 }
    )

    LaunchedEffect(pagerState.currentPage) {
        val newTab = when (pagerState.currentPage) {
            0 -> MainTab.NOTES
            1 -> MainTab.HABITS
            else -> MainTab.TASKS
        }
        if (selectedTab != newTab) {
            viewModel.setSelectedTab(newTab)
        }
    }

    LaunchedEffect(selectedTab) {
        val targetPage = when (selectedTab) {
            MainTab.NOTES -> 0
            MainTab.HABITS -> 1
            MainTab.TASKS -> 2
        }
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    // Handle Glance Widget Quick Actions
    LaunchedEffect(quickAction) {
        when (quickAction) {
            StreakGlanceWidget.ACTION_ADD_NOTE -> {
                viewModel.setSelectedTab(MainTab.NOTES)
                noteToEdit = null
                showNoteEditor = true
            }
            StreakGlanceWidget.ACTION_CHECK_HABIT -> {
                viewModel.setSelectedTab(MainTab.HABITS)
                viewModel.checkHabitDirectly("Quick Habit Completion")
                Toast.makeText(context, "Habit checked! Streak preserved.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val pendingCount = rawTasks.count { !it.isCompleted }
    val deadlineCount = rawTasks.count { !it.isCompleted && it.dueDateMillis != null }
    val completedCount = rawTasks.count { it.isCompleted }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                currentDestination = currentDrawerDestination,
                onDestinationSelect = { destination ->
                    currentDrawerDestination = destination
                    scope.launch { drawerState.close() }
                    when (destination) {
                        DrawerDestination.DASHBOARD -> viewModel.setSelectedTab(MainTab.HABITS)
                        DrawerDestination.DAILY_LOG -> showDailyLogSheet = true
                        DrawerDestination.CALENDAR_VIEW -> showCalendarSheet = true
                        DrawerDestination.HABIT_STACKS -> showHabitStacksSheet = true
                        DrawerDestination.ADD_HABIT -> showAddHabitSheet = true
                        DrawerDestination.INTERFACE_SETTINGS -> showInterfaceSheet = true
                        DrawerDestination.HABIT_TIPS -> showHabitTipsSheet = true
                        DrawerDestination.DATA_SYNC_BACKUP -> showBackupSheet = true
                        DrawerDestination.ABOUT_US -> showAboutUsDialog = true
                    }
                }
            )
        }
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = DarkBackground,
            bottomBar = {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = TextPrimary,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = pagerState.currentPage == 0,
                        onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                        icon = { Icon(Icons.Outlined.Description, contentDescription = "Notes") },
                        label = { Text("Notes") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentMint,
                            selectedTextColor = AccentMint,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = DarkSurfaceContainer
                        ),
                        modifier = Modifier.testTag("nav_notes")
                    )
                    NavigationBarItem(
                        selected = pagerState.currentPage == 1,
                        onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                        icon = { DashedClockIcon(tint = if (pagerState.currentPage == 1) AccentMint else TextSecondary) },
                        label = { Text("Habits") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentMint,
                            selectedTextColor = AccentMint,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = DarkSurfaceContainer
                        ),
                        modifier = Modifier.testTag("nav_habits")
                    )
                    NavigationBarItem(
                        selected = pagerState.currentPage == 2,
                        onClick = { scope.launch { pagerState.animateScrollToPage(2) } },
                        icon = { Icon(Icons.Outlined.CheckCircle, contentDescription = "Tasks") },
                        label = { Text("Tasks") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentMint,
                            selectedTextColor = AccentMint,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = DarkSurfaceContainer
                        ),
                        modifier = Modifier.testTag("nav_tasks")
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        when (pagerState.currentPage) {
                            0 -> {
                                noteToEdit = null
                                showNoteEditor = true
                            }
                            1 -> {
                                showAddHabitSheet = true
                            }
                            2 -> {
                                taskToEdit = null
                                showTaskEditor = true
                            }
                        }
                    },
                    containerColor = AccentMint,
                    contentColor = DarkBackground,
                    shape = CircleShape,
                    modifier = Modifier.testTag("add_item_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Item",
                        modifier = Modifier.size(26.dp)
                    )
                }
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Top App Header (No search icon, no production build text)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("hamburger_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Navigation Menu",
                                tint = TextPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "AMINIMAL",
                            color = TextPrimary,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showOptionsMenu = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("notification_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "Menu",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false },
                            modifier = Modifier.background(DarkSurface)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Settings",
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Settings,
                                        contentDescription = "Settings",
                                        tint = AccentMint,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    showSettingsDialog = true
                                },
                                modifier = Modifier.testTag("menu_settings_button")
                            )
                        }
                    }
                }

                // Horizontal Pager for 3 Tabs: 0 -> Notes, 1 -> Habits, 2 -> Tasks
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->
                    when (page) {
                        0 -> {
                            NotesTabContent(
                                notes = filteredNotes,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                customTags = customTags,
                                selectedTag = selectedNoteTag,
                                onSelectTag = { viewModel.setSelectedNoteTag(it) },
                                onEditNote = { note ->
                                    noteToEdit = note
                                    showNoteEditor = true
                                },
                                onTogglePin = { note -> viewModel.toggleNotePin(note) },
                                onDeleteNote = { note ->
                                    viewModel.deleteNote(note)
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Note \"${note.title}\" deleted",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.restoreLastDeletedNote()
                                        }
                                    }
                                }
                            )
                        }
                        1 -> {
                            HabitsTabContent(
                                habits = habits,
                                streakState = streakState,
                                isTodayActive = isTodayActive,
                                recentActivityLogs = recentActivityLogs,
                                onToggleHabit = { habitId -> viewModel.completeHabitDirectly(habitId) },
                                onOpenDetail = { habit -> selectedHabitForDetail = habit },
                                onPurchaseFreeze = { viewModel.purchaseStreakFreeze() },
                                onOpenStacks = { showHabitStacksSheet = true }
                            )
                        }
                        2 -> {
                            TasksTabContent(
                                tasks = filteredTasks,
                                taskFilter = taskFilter,
                                pendingCount = pendingCount,
                                deadlineCount = deadlineCount,
                                completedCount = completedCount,
                                onSelectFilter = { viewModel.setTaskFilter(it) },
                                onToggleComplete = { task -> viewModel.toggleTaskCompleted(task) },
                                onEditTask = { task ->
                                    taskToEdit = task
                                    showTaskEditor = true
                                },
                                onDeleteTask = { task ->
                                    viewModel.deleteTask(task)
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Task \"${task.title}\" deleted",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.restoreLastDeletedTask()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs & Sheets
    if (showTaskEditor) {
        TaskEditorSheet(
            taskToEdit = taskToEdit,
            onDismiss = { showTaskEditor = false },
            onSave = { title, description, dueDateMillis, priority, hasReminder ->
                if (taskToEdit == null) {
                    viewModel.addTask(title, description, dueDateMillis, priority, hasReminder)
                } else {
                    viewModel.updateTask(
                        taskToEdit!!.copy(
                            title = title,
                            description = description,
                            dueDateMillis = dueDateMillis,
                            priority = priority,
                            hasReminder = hasReminder
                        )
                    )
                }
            }
        )
    }

    if (showNoteEditor) {
        NoteEditorSheet(
            noteToEdit = noteToEdit,
            availableTags = customTags,
            onAddNewCustomTag = { viewModel.addCustomTag(it) },
            onDismiss = { showNoteEditor = false },
            onSave = { title, content, tag, isPinned ->
                if (noteToEdit == null) {
                    viewModel.addNote(title, content, tag, isPinned)
                } else {
                    viewModel.updateNote(
                        noteToEdit!!.copy(
                            title = title,
                            content = content,
                            tag = tag,
                            isPinned = isPinned
                        )
                    )
                }
            }
        )
    }

    if (selectedHabitForDetail != null) {
        HabitDetailSheet(
            habit = selectedHabitForDetail!!,
            onDismissRequest = { selectedHabitForDetail = null },
            onToggleCheckIn = { habitId -> viewModel.completeHabitDirectly(habitId) },
            onDeleteHabit = { habitId ->
                viewModel.deleteHabit(habitId)
                selectedHabitForDetail = null
            },
            onUpdateHabit = { habit -> viewModel.updateHabit(habit) }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            currentTheme = themeConfig,
            onSelectTheme = { viewModel.setThemeConfig(it) },
            streakReminderEnabled = streakReminderEnabled,
            onToggleStreakReminder = { viewModel.setStreakReminderEnabled(it) },
            badges = badges,
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showAboutUsDialog) {
        AboutUsDialog(onDismissRequest = { showAboutUsDialog = false })
    }

    if (showHabitStacksSheet) {
        HabitStacksSheet(
            habits = habits,
            onToggleStep = { habitId, stepId -> viewModel.toggleHabitStep(habitId, stepId) },
            onUpdateStepCount = { habitId, stepId, delta -> viewModel.updateHabitStepCount(habitId, stepId, delta) },
            onUpdateCount = { habitId, delta -> viewModel.updateHabitCount(habitId, delta) },
            onCompleteDirectly = { habitId -> viewModel.completeHabitDirectly(habitId) },
            onAddHabitStack = { title, anchor, steps, freq -> viewModel.addHabitStack(title, anchor, steps, freq) },
            onAddHabitItem = { habit -> viewModel.addHabitItem(habit) },
            onUpdateHabit = { habit -> viewModel.updateHabit(habit) },
            onDeleteHabit = { habitId -> viewModel.deleteHabit(habitId) },
            onDuplicateHabit = { habitId -> viewModel.duplicateHabit(habitId) },
            onTogglePauseHabit = { habitId -> viewModel.togglePauseHabit(habitId) },
            onResetTodayProgress = { habitId -> viewModel.resetHabitTodayProgress(habitId) },
            onDismissRequest = { showHabitStacksSheet = false }
        )
    }

    if (showAddHabitSheet) {
        AddHabitSheet(
            onDismissRequest = { showAddHabitSheet = false },
            onAddHabit = { habit -> viewModel.addHabitItem(habit) }
        )
    }

    if (showDailyLogSheet) {
        DailyLogSheet(activityLogs = recentActivityLogs, onDismissRequest = { showDailyLogSheet = false })
    }

    if (showCalendarSheet) {
        CalendarViewSheet(tasks = rawTasks, onDismissRequest = { showCalendarSheet = false })
    }

    if (showInterfaceSheet) {
        InterfaceSettingsSheet(
            fontSize = fontSize,
            onFontSizeChange = { viewModel.setFontSize(it) },
            sortingPreference = sortingPreference,
            onSortingChange = { viewModel.setSortingPreference(it) },
            confettiDisabled = confettiDisabled,
            onConfettiToggle = { viewModel.setConfettiDisabled(it) },
            stickyNotifications = stickyNotifications,
            onStickyToggle = { viewModel.setStickyNotifications(it) },
            firstDayOfWeek = firstDayOfWeek,
            onFirstDayChange = { viewModel.setFirstDayOfWeek(it) },
            iconBadgesEnabled = iconBadgesEnabled,
            onIconBadgesToggle = { viewModel.setIconBadgesEnabled(it) },
            onDismissRequest = { showInterfaceSheet = false }
        )
    }

    if (showHabitTipsSheet) {
        HabitTipsSheet(onDismissRequest = { showHabitTipsSheet = false })
    }

    if (showBackupSheet) {
        DataSyncBackupSheet(
            getJsonData = { viewModel.exportDataJson() },
            getCsvData = { viewModel.exportDataCsv() },
            onImportJson = { json -> viewModel.importDataJson(json) },
            onDismissRequest = { showBackupSheet = false }
        )
    }

    ConfettiOverlay(triggerKey = confettiTrigger)
}

@Composable
fun NotesTabContent(
    notes: List<NoteEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    customTags: List<String>,
    selectedTag: String?,
    onSelectTag: (String?) -> Unit,
    onEditNote: (NoteEntity) -> Unit,
    onTogglePin: (NoteEntity) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Notes",
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search notes...", color = TextMuted) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentMint,
                unfocusedBorderColor = DarkBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = AccentMint,
                focusedContainerColor = DarkSurfaceContainer,
                unfocusedContainerColor = DarkSurfaceContainer
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("notes_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChipItem(
                    label = "All",
                    count = notes.size,
                    isSelected = selectedTag == null,
                    onClick = { onSelectTag(null) },
                    testTag = "filter_tag_all"
                )
            }
            items(customTags) { tag ->
                FilterChipItem(
                    label = tag,
                    count = notes.count { it.tag.equals(tag, true) },
                    isSelected = selectedTag.equals(tag, true),
                    onClick = { onSelectTag(tag) },
                    testTag = "filter_tag_$tag"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (notes.isEmpty()) {
            EmptyStateView(
                icon = Icons.Outlined.Description,
                title = "No Notes Found",
                subtitle = "Tap the + button to draft a minimalist note."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = notes,
                    key = { it.id }
                ) { note ->
                    NoteItemCard(
                        note = note,
                        onEdit = { onEditNote(note) },
                        onTogglePin = { onTogglePin(note) },
                        onDelete = { onDeleteNote(note) }
                    )
                }
            }
        }
    }
}

@Composable
fun HabitsTabContent(
    habits: List<HabitItem>,
    streakState: com.example.data.UserStreakState,
    isTodayActive: Boolean,
    recentActivityLogs: List<com.example.data.ActivityLog>,
    onToggleHabit: (String) -> Unit,
    onOpenDetail: (HabitItem) -> Unit,
    onPurchaseFreeze: () -> Unit,
    onOpenStacks: () -> Unit
) {
    val remainingCount = habits.count { !it.isCompletedToday && !it.isPaused }
    val subtitleText = if (remainingCount == 0) "All habits completed today!" else "$remainingCount Habits remaining today"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Habits",
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitleText,
            color = TextMuted,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        StreakHeader(
            streakState = streakState,
            isTodayActive = isTodayActive,
            recentLogs = recentActivityLogs,
            onPurchaseFreeze = onPurchaseFreeze,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceVariant)
                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                .clickable { onOpenStacks() }
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("habit_stacks_banner"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    tint = AccentMint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Habit Stacks",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${habits.size} active stacks • Tap to manage",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
            Text(
                text = "Open ➔",
                color = AccentMint,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (habits.isEmpty()) {
            EmptyStateView(
                icon = Icons.Outlined.CheckCircle,
                title = "No Habits Yet",
                subtitle = "Tap the + button to add a new habit."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = habits,
                    key = { it.id }
                ) { habit ->
                    HabitCardItem(
                        habit = habit,
                        onToggle = { onToggleHabit(habit.id) },
                        onClick = { onOpenDetail(habit) }
                    )
                }
            }
        }
    }
}

@Composable
fun HabitCardItem(
    habit: HabitItem,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceContainer)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(16.dp)
            .testTag("habit_item_${habit.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = habit.title,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                val metadata = if (habit.anchorAction.isNotBlank()) "Daily | ${habit.anchorAction}" else "Daily | Morning"
                Text(
                    text = metadata,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (habit.currentStreak > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🔥 ${habit.currentStreak} day streak",
                        color = com.example.ui.theme.AccentAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Switch(
                checked = habit.isCompletedToday,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = DarkBackground,
                    checkedTrackColor = AccentMint,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = DarkSurface,
                    uncheckedBorderColor = DarkBorder
                ),
                modifier = Modifier.testTag("habit_switch_${habit.id}")
            )
        }
    }
}

@Composable
fun TasksTabContent(
    tasks: List<TaskEntity>,
    taskFilter: TaskFilter,
    pendingCount: Int,
    deadlineCount: Int,
    completedCount: Int,
    onSelectFilter: (TaskFilter) -> Unit,
    onToggleComplete: (TaskEntity) -> Unit,
    onEditTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Tasks",
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "$pendingCount Pending tasks",
            color = TextMuted,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChipItem(
                    label = "All",
                    count = tasks.size,
                    isSelected = taskFilter == TaskFilter.ALL,
                    onClick = { onSelectFilter(TaskFilter.ALL) },
                    testTag = "filter_all"
                )
            }
            item {
                FilterChipItem(
                    label = "Pending",
                    count = pendingCount,
                    isSelected = taskFilter == TaskFilter.PENDING,
                    onClick = { onSelectFilter(TaskFilter.PENDING) },
                    testTag = "filter_pending"
                )
            }
            item {
                FilterChipItem(
                    label = "Deadlines",
                    count = deadlineCount,
                    isSelected = taskFilter == TaskFilter.DEADLINES,
                    onClick = { onSelectFilter(TaskFilter.DEADLINES) },
                    testTag = "filter_deadlines",
                    accentColor = if (deadlineCount > 0) com.example.ui.theme.AccentCoral else null
                )
            }
            item {
                FilterChipItem(
                    label = "Completed",
                    count = completedCount,
                    isSelected = taskFilter == TaskFilter.COMPLETED,
                    onClick = { onSelectFilter(TaskFilter.COMPLETED) },
                    testTag = "filter_completed"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (tasks.isEmpty()) {
            EmptyStateView(
                icon = Icons.Outlined.CheckCircle,
                title = "No Tasks Found",
                subtitle = "Tap the + button to add a new task."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = tasks,
                    key = { it.id }
                ) { task ->
                    TaskItemCard(
                        task = task,
                        onToggleComplete = { onToggleComplete(task) },
                        onEdit = { onEditTask(task) },
                        onDelete = { onDeleteTask(task) }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChipItem(
    label: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    accentColor: Color? = null
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) DarkSurfaceContainer else DarkSurface)
            .border(
                1.dp,
                if (isSelected) (accentColor ?: AccentMint) else DarkBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                color = if (isSelected) (accentColor ?: AccentMint) else TextSecondary,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$count",
                color = if (isSelected) TextPrimary else TextMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun EmptyStateView(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
                    .border(1.dp, DarkBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
