package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ActivityLog
import com.example.data.UserStreakState
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentSky
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.time.DayOfWeek
import java.time.LocalDate

data class DayActivitySummary(
    val dayName: String,
    val date: LocalDate,
    val count: Int,
    val isToday: Boolean
)

/**
 * Visualizes the user's weekly activity and streak history progress with a native Compose bar chart.
 */
@Composable
fun StreakStatistics(
    streakState: UserStreakState,
    recentLogs: List<ActivityLog>,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val currentStreak = streakState.currentStreak
    val highestStreak = maxOf(streakState.highestStreak, currentStreak)

    // Calculate last 7 days (Monday to Sunday or past 7 days)
    val weekSummaries = remember(recentLogs, today) {
        val startOfWeek = today.minusDays(6)
        (0..6).map { dayOffset ->
            val date = startOfWeek.plusDays(dayOffset.toLong())
            val dateStr = date.toString()
            val count = recentLogs.count { it.date == dateStr }
            val dayName = when (date.dayOfWeek) {
                DayOfWeek.MONDAY -> "M"
                DayOfWeek.TUESDAY -> "T"
                DayOfWeek.WEDNESDAY -> "W"
                DayOfWeek.THURSDAY -> "T"
                DayOfWeek.FRIDAY -> "F"
                DayOfWeek.SATURDAY -> "S"
                DayOfWeek.SUNDAY -> "S"
            }
            DayActivitySummary(
                dayName = dayName,
                date = date,
                count = count,
                isToday = date == today
            )
        }
    }

    val activeDaysThisWeek = weekSummaries.count { it.count > 0 }
    val maxCountInWeek = maxOf(1, weekSummaries.maxOf { it.count })

    // Bar animation progress
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.BarChart,
                    contentDescription = null,
                    tint = AccentMint,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Weekly Activity & Progress",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "$activeDaysThisWeek/7 Days Active",
                color = AccentMint,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Native Bar Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(90.dp)) {
                val totalBars = weekSummaries.size
                val barWidth = 18.dp.toPx()
                val totalAvailableWidth = size.width
                val spacing = (totalAvailableWidth - (totalBars * barWidth)) / (totalBars + 1)
                val chartHeight = size.height

                // Draw background grid lines
                val gridLineCount = 3
                for (i in 0..gridLineCount) {
                    val y = chartHeight * (i.toFloat() / gridLineCount)
                    drawLine(
                        color = Color(0xFF262C3A).copy(alpha = 0.5f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                }

                // Draw bars
                weekSummaries.forEachIndexed { index, day ->
                    val x = spacing + index * (barWidth + spacing)
                    val rawFraction = (day.count.toFloat() / maxCountInWeek).coerceIn(0.12f, 1f)
                    val animatedHeight = chartHeight * rawFraction * animProgress.value
                    val y = chartHeight - animatedHeight

                    val barBrush = when {
                        day.count > 0 && day.isToday -> Brush.verticalGradient(
                            listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                        )
                        day.count > 0 -> Brush.verticalGradient(
                            listOf(Color(0xFF34D399), Color(0xFF059669))
                        )
                        else -> Brush.verticalGradient(
                            listOf(Color(0xFF2B3242), Color(0xFF1E232F))
                        )
                    }

                    drawRoundRect(
                        brush = barBrush,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, animatedHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }

            // Day Labels Row below the chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                weekSummaries.forEach { day ->
                    Text(
                        text = day.dayName,
                        color = if (day.isToday) AccentAmber else if (day.count > 0) TextPrimary else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary Metric Badges Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricPill(
                title = "Current",
                value = "$currentStreak d",
                color = AccentAmber,
                modifier = Modifier.weight(1f)
            )
            MetricPill(
                title = "Longest",
                value = "$highestStreak d",
                color = AccentMint,
                modifier = Modifier.weight(1f)
            )
            MetricPill(
                title = "Safety Net",
                value = "${streakState.streakFreezesCount}/2 ❄️",
                color = AccentSky,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricPill(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceContainer)
            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title.uppercase(),
                color = TextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = color,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
