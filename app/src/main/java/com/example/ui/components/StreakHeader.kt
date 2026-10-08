package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserStreakState
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentSky
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.time.LocalDate

/**
 * Modern Flame Icon Header inspired by the Elevate app streak & safety net architecture.
 * Displays:
 * 1. Animated glowing flame icon
 * 2. Current streak count + status badge
 * 3. Two safety net streak freeze shields (available vs consumed)
 * 4. Tap affordance to open detailed streak breakdown, rules, and freeze replenishment
 */
@Composable
fun StreakHeader(
    streakState: UserStreakState,
    isTodayActive: Boolean,
    recentLogs: List<com.example.data.ActivityLog> = emptyList(),
    onPurchaseFreeze: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDetailsSheet by remember { mutableStateOf(false) }

    val currentStreak = streakState.currentStreak
    val freezes = streakState.streakFreezesCount

    // Pulsing animation for active streak flame
    val infiniteTransition = rememberInfiniteTransition(label = "flame_pulse")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = if (currentStreak > 0) 1.08f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )

    val flameColor by animateColorAsState(
        targetValue = when {
            currentStreak >= 7 -> Color(0xFFFF5722) // High streak deep flame
            currentStreak > 0 -> Color(0xFFF59E0B)  // Active amber flame
            freezes > 0 -> AccentSky                // Protected by ice freezes
            else -> TextMuted
        },
        label = "flame_color"
    )

    // Header Card
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        DarkSurfaceVariant,
                        DarkSurface
                    )
                )
            )
            .border(
                1.dp,
                if (currentStreak > 0) AccentAmber.copy(alpha = 0.35f) else DarkBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable { showDetailsSheet = true }
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("streak_header")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Flame Icon + Streak Count
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (currentStreak > 0)
                                flameColor.copy(alpha = 0.15f)
                            else
                                DarkSurfaceContainer
                        )
                        .border(
                            1.dp,
                            if (currentStreak > 0) flameColor.copy(alpha = 0.4f) else DarkBorderSubtle,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Whatshot,
                        contentDescription = "Streak flame",
                        tint = flameColor,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(if (currentStreak > 0) flameScale else 1f)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$currentStreak",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentStreak == 1) "DAY STREAK" else "DAYS STREAK",
                            color = if (currentStreak > 0) AccentAmber else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = when {
                            isTodayActive -> "✓ Completed for today"
                            currentStreak > 0 -> "Log habit or note to extend"
                            else -> "Ignite your streak today"
                        },
                        color = if (isTodayActive) AccentMint else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Right: Safety Net Freezes (2 slots)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Freeze slot 1
                FreezeBadge(isAvailable = freezes >= 1, slot = 1)
                // Freeze slot 2
                FreezeBadge(isAvailable = freezes >= 2, slot = 2)
            }
        }
    }

    // Elevate-style Interactive Streak Details Sheet
    if (showDetailsSheet) {
        StreakDetailsBottomSheet(
            streakState = streakState,
            isTodayActive = isTodayActive,
            recentLogs = recentLogs,
            onDismiss = { showDetailsSheet = false },
            onPurchaseFreeze = onPurchaseFreeze
        )
    }
}

/**
 * Visual badge representing one safety net freeze slot.
 */
@Composable
fun FreezeBadge(
    isAvailable: Boolean,
    slot: Int
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isAvailable) AccentSky.copy(alpha = 0.18f) else DarkSurfaceContainer)
            .border(
                1.dp,
                if (isAvailable) AccentSky.copy(alpha = 0.5f) else DarkBorder,
                RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isAvailable) Icons.Outlined.AcUnit else Icons.Outlined.Shield,
            contentDescription = if (isAvailable) "Freeze available (slot $slot)" else "Freeze consumed (slot $slot)",
            tint = if (isAvailable) AccentSky else TextMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StreakDetailsBottomSheet(
    streakState: UserStreakState,
    isTodayActive: Boolean,
    recentLogs: List<com.example.data.ActivityLog>,
    onDismiss: () -> Unit,
    onPurchaseFreeze: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val currentStreak = streakState.currentStreak
    val freezes = streakState.streakFreezesCount
    val coins = streakState.coins

    // Next milestone calculation
    val milestones = listOf(3, 7, 14, 30, 60, 100)
    val nextMilestone = milestones.firstOrNull { it > currentStreak } ?: 100
    val prevMilestone = milestones.lastOrNull { it <= currentStreak } ?: 0
    val progressFraction = if (nextMilestone > prevMilestone) {
        ((currentStreak - prevMilestone).toFloat() / (nextMilestone - prevMilestone)).coerceIn(0f, 1f)
    } else 1f

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "STREAK & SAFETY NET",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Streaks",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Hero Card: Streak Showcase
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceVariant)
                    .border(1.dp, AccentAmber.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(AccentAmber.copy(alpha = 0.15f))
                            .border(1.dp, AccentAmber.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Whatshot,
                            contentDescription = null,
                            tint = AccentAmber,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "$currentStreak",
                        color = TextPrimary,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = if (currentStreak == 1) "Active Day" else "Consecutive Active Days",
                        color = AccentAmber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Best: ${streakState.highestStreak} days",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "•",
                            color = TextMuted
                        )
                        Text(
                            text = "Coins: $coins",
                            color = AccentAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Safety Net (Freezes) Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceContainer)
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.AcUnit,
                                contentDescription = null,
                                tint = AccentSky,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Safety Net Freezes",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AccentSky.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$freezes / ${UserStreakState.MAX_FREEZES} Available",
                                color = AccentSky,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Missing a calendar day automatically consumes 1 Streak Freeze to protect your streak. If all freezes are exhausted and a day is missed, the streak resets to 0.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Freeze slot visualizer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (i in 1..UserStreakState.MAX_FREEZES) {
                            val isAvailable = freezes >= i
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isAvailable) AccentSky.copy(alpha = 0.12f) else DarkSurface
                                    )
                                    .border(
                                        1.dp,
                                        if (isAvailable) AccentSky.copy(alpha = 0.4f) else DarkBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isAvailable) Icons.Outlined.AcUnit else Icons.Outlined.Shield,
                                        contentDescription = null,
                                        tint = if (isAvailable) AccentSky else TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isAvailable) "Slot $i: Ready" else "Slot $i: Consumed",
                                        color = if (isAvailable) AccentSky else TextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Replenish with coins button if below capacity
                    if (freezes < UserStreakState.MAX_FREEZES) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onPurchaseFreeze,
                            enabled = coins >= UserStreakState.FREEZE_COIN_COST,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentSky,
                                contentColor = DarkBackground,
                                disabledContainerColor = DarkSurface,
                                disabledContentColor = TextMuted
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("purchase_freeze_button")
                        ) {
                            Text(
                                text = "Purchase Freeze (${UserStreakState.FREEZE_COIN_COST} Coins)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Milestone Roadmap
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceVariant)
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Next Reward: $nextMilestone Days",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "+1 Freeze Earned",
                            color = AccentMint,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = AccentAmber,
                        trackColor = DarkSurfaceContainer
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(3, 7, 14, 30).forEach { m ->
                            val isReached = currentStreak >= m
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isReached) Icons.Default.Check else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (isReached) AccentMint else TextMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "$m d",
                                    color = if (isReached) TextPrimary else TextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = if (isReached) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Elevate Rules Checklist
            Text(
                text = "HOW STREAKS WORK",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            RuleItem(
                icon = Icons.Outlined.CheckCircle,
                title = "Complete or Add a Habit",
                description = "Either action counts as active for that calendar day."
            )
            RuleItem(
                icon = Icons.Outlined.Edit,
                title = "1 Active Day Maximum",
                description = "Completing multiple habits/notes on the same day counts as 1 streak day."
            )
            RuleItem(
                icon = Icons.Outlined.AcUnit,
                title = "Safety Net Protection",
                description = "Missing a day uses 1 freeze. Recovers to 2 freezes after 2 consecutive days post-reset."
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Weekly Streak & Activity Statistics Chart
            StreakStatistics(
                streakState = streakState,
                recentLogs = recentLogs
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RuleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentMint,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                color = TextMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
