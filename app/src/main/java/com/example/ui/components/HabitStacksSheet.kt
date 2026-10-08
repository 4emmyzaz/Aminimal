package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HabitFrequency
import com.example.data.HabitItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitStacksSheet(
    habits: List<HabitItem>,
    onToggleStep: (habitId: String, stepId: String) -> Unit,
    onUpdateCount: (habitId: String, delta: Int) -> Unit,
    onCompleteDirectly: (habitId: String) -> Unit,
    onAddHabitStack: ((title: String, anchorAction: String, steps: List<String>, frequency: HabitFrequency) -> Unit)? = null,
    onUpdateStepCount: ((habitId: String, stepId: String, delta: Int) -> Unit)? = null,
    onAddHabitItem: ((HabitItem) -> Unit)? = null,
    onUpdateHabit: ((HabitItem) -> Unit)? = null,
    onDeleteHabit: ((habitId: String) -> Unit)? = null,
    onDuplicateHabit: ((habitId: String) -> Unit)? = null,
    onTogglePauseHabit: ((habitId: String) -> Unit)? = null,
    onResetTodayProgress: ((habitId: String) -> Unit)? = null,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showCreateSheet by remember { mutableStateOf(false) }
    var showAddHabitSheet by remember { mutableStateOf(false) }
    var selectedHabitDetail by remember { mutableStateOf<HabitItem?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("habit_stacks_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp)
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header: Title & Subtitle + Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Habit Stacks",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Anchored Sequential Routines",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.testTag("close_habit_stacks_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons Row (Create Stack & Add Habit)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showCreateSheet = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("create_new_habit_stack_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "New Stack", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }

                Button(
                    onClick = { showAddHabitSheet = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("add_habit_button_sheet"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Add Habit", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Habits List with Fast Quick Check-In & Tappable Details
            Text(
                text = "Active Habits (${habits.size})",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(habits) { habit ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedHabitDetail = habit },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = habit.emoji.ifBlank { "💪" }, fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = habit.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFF97316), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "${habit.currentStreak}d streak • ${habit.category}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Fast Quick Check-In Circle
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (habit.isCompletedToday) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (habit.isCompletedToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = CircleShape
                                    )
                                    .clickable { onCompleteDirectly(habit.id) }
                                    .testTag("habit_checkin_${habit.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (habit.isCompletedToday) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Completed",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create New Habit Stack Multi-Step Flow
    if (showCreateSheet) {
        CreateHabitStackSheet(
            onDismissRequest = { showCreateSheet = false },
            onCreateHabit = { newHabit ->
                if (onAddHabitItem != null) {
                    onAddHabitItem(newHabit)
                } else if (onAddHabitStack != null) {
                    onAddHabitStack(
                        newHabit.title,
                        newHabit.anchorAction,
                        newHabit.sequentialSteps.map { it.title },
                        newHabit.frequency
                    )
                }
            }
        )
    }

    // Add Habit Sheet
    if (showAddHabitSheet) {
        AddHabitSheet(
            onDismissRequest = { showAddHabitSheet = false },
            onAddHabit = { newHabit ->
                onAddHabitItem?.invoke(newHabit)
            }
        )
    }

    // Habit Detail Sheet
    selectedHabitDetail?.let { habit ->
        HabitDetailSheet(
            habit = habit,
            onDismissRequest = { selectedHabitDetail = null },
            onToggleCheckIn = { habitId ->
                onCompleteDirectly(habitId)
                selectedHabitDetail = habits.find { it.id == habitId }
            },
            onDeleteHabit = { habitId ->
                onDeleteHabit?.invoke(habitId)
                selectedHabitDetail = null
            },
            onUpdateHabit = { updated ->
                onUpdateHabit?.invoke(updated)
                selectedHabitDetail = updated
            }
        )
    }
}
