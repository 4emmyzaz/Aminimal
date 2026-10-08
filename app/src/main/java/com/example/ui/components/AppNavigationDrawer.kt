package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class DrawerDestination {
    DASHBOARD,
    DAILY_LOG,
    CALENDAR_VIEW,
    HABIT_STACKS,
    ADD_HABIT,
    INTERFACE_SETTINGS,
    HABIT_TIPS,
    DATA_SYNC_BACKUP,
    ABOUT_US
}

@Composable
fun AppDrawerContent(
    currentDestination: DrawerDestination,
    onDestinationSelect: (DrawerDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier
            .width(320.dp)
            .fillMaxHeight(),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header Section: User Profile
            DrawerProfileHeader()

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Categorized Items
            DrawerSectionHeader(title = "Core Tracking")
            DrawerItemRow(
                label = "Dashboard",
                icon = Icons.Default.Dashboard,
                selected = currentDestination == DrawerDestination.DASHBOARD,
                testTag = "drawer_item_dashboard",
                onClick = { onDestinationSelect(DrawerDestination.DASHBOARD) }
            )
            DrawerItemRow(
                label = "Daily Log",
                icon = Icons.Default.History,
                selected = currentDestination == DrawerDestination.DAILY_LOG,
                testTag = "drawer_item_daily_log",
                onClick = { onDestinationSelect(DrawerDestination.DAILY_LOG) }
            )
            DrawerItemRow(
                label = "Calendar View",
                icon = Icons.Default.CalendarMonth,
                selected = currentDestination == DrawerDestination.CALENDAR_VIEW,
                testTag = "drawer_item_calendar",
                onClick = { onDestinationSelect(DrawerDestination.CALENDAR_VIEW) }
            )

            DrawerSectionHeader(title = "Routines")
            DrawerItemRow(
                label = "Habit Stacks",
                icon = Icons.Default.Layers,
                selected = currentDestination == DrawerDestination.HABIT_STACKS,
                badgeText = "Anchored",
                testTag = "drawer_item_stacks",
                onClick = { onDestinationSelect(DrawerDestination.HABIT_STACKS) }
            )
            DrawerItemRow(
                label = "Add Habit",
                icon = Icons.Default.Add,
                selected = currentDestination == DrawerDestination.ADD_HABIT,
                testTag = "drawer_item_add_habit",
                onClick = { onDestinationSelect(DrawerDestination.ADD_HABIT) }
            )

             DrawerSectionHeader(title = "Preferences")
            DrawerItemRow(
                label = "Interface",
                icon = Icons.Default.Tune,
                selected = currentDestination == DrawerDestination.INTERFACE_SETTINGS,
                testTag = "drawer_item_interface",
                onClick = { onDestinationSelect(DrawerDestination.INTERFACE_SETTINGS) }
            )
            DrawerItemRow(
                label = "Habit Tips",
                icon = Icons.Default.Lightbulb,
                selected = currentDestination == DrawerDestination.HABIT_TIPS,
                testTag = "drawer_item_habit_tips",
                onClick = { onDestinationSelect(DrawerDestination.HABIT_TIPS) }
            )
            DrawerItemRow(
                label = "Data / Backup",
                icon = Icons.Default.Backup,
                selected = currentDestination == DrawerDestination.DATA_SYNC_BACKUP,
                testTag = "drawer_item_backup",
                onClick = { onDestinationSelect(DrawerDestination.DATA_SYNC_BACKUP) }
            )

            DrawerSectionHeader(title = "Information")
            DrawerItemRow(
                label = "About Us",
                icon = Icons.Default.Info,
                selected = currentDestination == DrawerDestination.ABOUT_US,
                badgeText = "v1.5",
                testTag = "drawer_item_about",
                onClick = { onDestinationSelect(DrawerDestination.ABOUT_US) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DrawerProfileHeader() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User Avatar Circle
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "EA",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Emmyz Azuby",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Emmyz.azuby@gmail.com",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 6.dp)
    )
}

@Composable
private fun DrawerItemRow(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    badgeText: String? = null,
    testTag: String,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                )
                if (badgeText != null) {
                    Surface(
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(22.dp)
            )
        },
        selected = selected,
        onClick = onClick,
        modifier = Modifier
            .padding(NavigationDrawerItemDefaults.ItemPadding)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}
