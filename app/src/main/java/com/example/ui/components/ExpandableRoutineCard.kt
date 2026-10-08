package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HabitFrequency
import com.example.data.HabitItem
import com.example.data.HabitStep
import com.example.data.HabitStepType
import com.example.data.TriggerType
import kotlin.math.roundToInt

@Composable
fun ExpandableRoutineCard(
    habit: HabitItem,
    onToggleStep: (stepId: String) -> Unit,
    onUpdateStepCount: (stepId: String, delta: Int) -> Unit,
    onUpdateTopLevelCount: (delta: Int) -> Unit,
    onCompleteDirectly: () -> Unit,
    onEditRoutine: () -> Unit,
    onReorderHabits: () -> Unit,
    onDuplicateRoutine: () -> Unit,
    onTogglePauseRoutine: () -> Unit,
    onResetTodayProgress: () -> Unit,
    onDeleteRoutine: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var isStackActive by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "chevron_rotation"
    )

    // Calculate dynamic progress
    val completionPercentage = habit.completionPercentage
    val progressPercentInt = (completionPercentage * 100f).roundToInt()

    // Find the first incomplete step for Start Stack mode
    val firstIncompleteIndex = habit.sequentialSteps.indexOfFirst { !it.isStepCompleted }
    val currentActiveStepIndex = if (firstIncompleteIndex >= 0) firstIncompleteIndex else -1

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isStackActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { isExpanded = !isExpanded }
            .animateContentSize(animationSpec = tween(250))
            .testTag("routine_card_${habit.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // ──────────────────────────────────────────
            // 1. ANCHOR / TRIGGER HEADER
            // ──────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    val triggerIcon = getTriggerIcon(habit.triggerType)
                    Icon(
                        imageVector = triggerIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "⚓ ANCHOR: ${habit.anchorAction}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (habit.isPaused) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "PAUSED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (habit.isGraceWindowActive) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp),
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Grace Window",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ──────────────────────────────────────────
            // 2. ROUTINE NAME & STREAK
            // ──────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    color = Color(0xFFF97316).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "🔥 ${habit.currentStreak}d",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFFB923C),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ──────────────────────────────────────────
            // 3. ROUTINE METADATA SUMMARY & EXPAND CHEVRON
            // ──────────────────────────────────────────
            val summaryText = when {
                habit.sequentialSteps.isNotEmpty() -> {
                    val habitCount = "${habit.sequentialSteps.size} habits"
                    val duration = if (habit.totalEstimatedMinutes > 0) " · ${habit.totalEstimatedMinutes} min" else ""
                    "$habitCount$duration"
                }
                habit.frequency == HabitFrequency.COUNT_BASED -> {
                    "1 habit · ${habit.targetCount} ${habit.unit} target"
                }
                else -> {
                    "1 habit · ${habit.targetWeeklyDays}x weekly"
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = summaryText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { isExpanded = !isExpanded },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse routine" else "Expand routine",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(chevronRotation)
                    )
                }
            }

            // ──────────────────────────────────────────
            // 4. PROGRESS BAR & PERCENTAGE
            // ──────────────────────────────────────────
            Spacer(modifier = Modifier.height(8.dp))

            if (habit.frequency == HabitFrequency.COUNT_BASED && habit.sequentialSteps.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${habit.currentCount} / ${habit.targetCount} ${habit.unit}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$progressPercentInt%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (habit.isEntirelyCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (habit.sequentialSteps.isNotEmpty()) {
                            "${habit.completedStepsCount} of ${habit.sequentialSteps.size} completed"
                        } else {
                            if (habit.isCompletedToday) "Completed today" else "Pending completion"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$progressPercentInt%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (habit.isEntirelyCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { completionPercentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = if (habit.isEntirelyCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )

            // ──────────────────────────────────────────
            // 5. EXPANDED CONTENT (ANIMATED VERTICALLY)
            // ──────────────────────────────────────────
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(250)) + fadeIn(animationSpec = tween(200)),
                exit = shrinkVertically(animationSpec = tween(250)) + fadeOut(animationSpec = tween(200))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        thickness = 1.dp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sequential steps header
                    Text(
                        text = "HABIT SEQUENCE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Render steps or quantitative controls
                    if (habit.sequentialSteps.isNotEmpty()) {
                        habit.sequentialSteps.forEachIndexed { index, step ->
                            val isCurrentStep = isStackActive && index == currentActiveStepIndex
                            val isLast = index == habit.sequentialSteps.lastIndex

                            SequentialHabitRow(
                                step = step,
                                stepIndex = index,
                                isLastStep = isLast,
                                isCurrentStep = isCurrentStep,
                                isStackActive = isStackActive,
                                onToggle = { onToggleStep(step.id) },
                                onUpdateCount = { delta -> onUpdateStepCount(step.id, delta) }
                            )
                        }
                    } else if (habit.frequency == HabitFrequency.COUNT_BASED) {
                        // Quantitative single habit row
                        QuantitativeControlsRow(
                            currentCount = habit.currentCount,
                            targetCount = habit.targetCount,
                            unit = habit.unit,
                            onUpdateCount = onUpdateTopLevelCount
                        )
                    } else {
                        // Direct daily completion button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Weekly goal: ${habit.targetWeeklyDays} days",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = onCompleteDirectly,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                if (habit.isCompletedToday) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Completed")
                                } else {
                                    Text("Mark Done")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Active stack completion message
                    if (isStackActive && currentActiveStepIndex == -1 && habit.sequentialSteps.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🎉", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Routine Complete!",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "All sequential habits completed in sequence.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // ──────────────────────────────────────────
                    // 6. ROUTINE ACTION BUTTONS
                    // ──────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Start Stack / Exit Stack
                        if (habit.sequentialSteps.isNotEmpty()) {
                            if (!isStackActive) {
                                Button(
                                    onClick = {
                                        isStackActive = true
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.testTag("start_stack_button_${habit.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Start Stack",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { isStackActive = false },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("exit_stack_button_${habit.id}")
                                ) {
                                    Text("Exit Stack Mode")
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        // Right: Three-dot action menu
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.testTag("routine_menu_button_${habit.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Routine Options",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit Routine") },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        onEditRoutine()
                                    }
                                )
                                if (habit.sequentialSteps.size > 1) {
                                    DropdownMenuItem(
                                        text = { Text("Reorder Habits") },
                                        leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                                        onClick = {
                                            showMenu = false
                                            onReorderHabits()
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Duplicate Routine") },
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        onDuplicateRoutine()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (habit.isPaused) "Resume Routine" else "Pause Routine") },
                                    leadingIcon = { Icon(if (habit.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        onTogglePauseRoutine()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Reset Today's Progress") },
                                    leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        onResetTodayProgress()
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Delete Routine",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        onDeleteRoutine()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Sequential Habit Row with vertical connector line to visualize continuity.
 */
@Composable
private fun SequentialHabitRow(
    step: HabitStep,
    stepIndex: Int,
    isLastStep: Boolean,
    isCurrentStep: Boolean,
    isStackActive: Boolean,
    onToggle: () -> Unit,
    onUpdateCount: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Step number indicator & vertical connector line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            // Circle node
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            step.isStepCompleted -> MaterialTheme.colorScheme.primary
                            isCurrentStep -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .border(
                        width = if (isCurrentStep) 2.dp else 1.dp,
                        color = when {
                            step.isStepCompleted -> MaterialTheme.colorScheme.primary
                            isCurrentStep -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (step.isStepCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        text = "${stepIndex + 1}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isCurrentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Connector line leading to the next step
            if (!isLastStep) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(34.dp)
                        .background(
                            if (step.isStepCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Step card content
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = when {
                isCurrentStep -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                step.isStepCompleted -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            },
            border = if (isCurrentStep) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
            else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onToggle() }
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Checkbox
                    Checkbox(
                        checked = step.isStepCompleted,
                        onCheckedChange = { onToggle() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Title
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isCurrentStep) FontWeight.Bold else FontWeight.Medium,
                            textDecoration = if (step.isStepCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = when {
                            step.isStepCompleted -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            isCurrentStep -> MaterialTheme.colorScheme.onSurface
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Duration badge or quantitative target
                    if (step.durationMinutes != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${step.durationMinutes}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (step.type != HabitStepType.BOOLEAN && step.targetValue > 1) {
                        Text(
                            text = "${step.currentValue}/${step.targetValue} ${step.unit}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // If step is Quantitative, show + / - controls
                if (step.type != HabitStepType.BOOLEAN && step.targetValue > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalIconButton(
                            onClick = { onUpdateCount(-1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${step.currentValue} ${step.unit}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalIconButton(
                            onClick = { onUpdateCount(1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // If Current Step during Start Stack mode, highlight prominently with a Complete button
                if (isCurrentStep && !step.isStepCompleted) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "▶ CURRENT STEP",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Button(
                            onClick = onToggle,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Complete Step", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Quantitative controls (+ / -) for single quantitative habits.
 */
@Composable
private fun QuantitativeControlsRow(
    currentCount: Int,
    targetCount: Int,
    unit: String,
    onUpdateCount: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "$currentCount / $targetCount $unit",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Target Goal: $targetCount $unit",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val delta = if (unit == "ml") 250 else 1
            FilledTonalIconButton(
                onClick = { onUpdateCount(-delta) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease")
            }
            FilledTonalIconButton(
                onClick = { onUpdateCount(delta) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase")
            }
        }
    }
}

private fun getTriggerIcon(triggerType: TriggerType): ImageVector {
    return when (triggerType) {
        TriggerType.TIME -> Icons.Default.Schedule
        TriggerType.EVENT -> Icons.Default.Anchor
        TriggerType.LOCATION -> Icons.Default.Anchor
        TriggerType.ENVIRONMENT -> Icons.Default.Visibility
        TriggerType.COMPLETION -> Icons.Default.Check
        TriggerType.MANUAL -> Icons.Default.PlayArrow
    }
}
