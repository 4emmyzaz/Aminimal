package com.example.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MainActivity
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Jetpack Glance Home Screen Widget displaying current streak, freeze safety net status,
 * and quick actions for Adding a Note or Checking a Habit.
 */
class StreakGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getDatabase(context)
        val streakState = db.streakDao().getStreakState()
        val currentStreak = streakState?.currentStreak ?: 0
        val freezes = streakState?.streakFreezesCount ?: 2

        provideContent {
            StreakWidgetContent(
                context = context,
                streak = currentStreak,
                freezes = freezes
            )
        }
    }

    companion object {
        const val EXTRA_QUICK_ACTION = "EXTRA_QUICK_ACTION"
        const val ACTION_ADD_NOTE = "ACTION_ADD_NOTE"
        const val ACTION_CHECK_HABIT = "ACTION_CHECK_HABIT"

        fun triggerUpdate(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val manager = GlanceAppWidgetManager(context)
                    val glanceIds = manager.getGlanceIds(StreakGlanceWidget::class.java)
                    glanceIds.forEach { id ->
                        StreakGlanceWidget().update(context, id)
                    }
                } catch (e: Throwable) {
                    // Ignore background failures
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun StreakWidgetContent(
    context: Context,
    streak: Int,
    freezes: Int
) {
    // Intents for quick actions
    val openAppIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    val addNoteIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(StreakGlanceWidget.EXTRA_QUICK_ACTION, StreakGlanceWidget.ACTION_ADD_NOTE)
    }

    val checkHabitIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(StreakGlanceWidget.EXTRA_QUICK_ACTION, StreakGlanceWidget.ACTION_CHECK_HABIT)
    }

    val bgDark = Color(0xFF101319)
    val cardDark = Color(0xFF1A1F2B)
    val accentMint = Color(0xFF34D399)
    val accentAmber = Color(0xFFFBBF24)
    val accentCyan = Color(0xFF38BDF8)
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgDark)
            .cornerRadius(16.dp)
            .padding(12.dp)
            .clickable(actionStartActivity(openAppIntent))
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Header Bar
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AMINIMAL",
                    style = TextStyle(
                        color = ColorProvider(textSecondary),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                // Safety net freeze indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "❄️ $freezes/2 Freezes",
                        style = TextStyle(
                            color = ColorProvider(if (freezes > 0) accentCyan else textSecondary),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Main Streak Highlight
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (streak > 0) "🔥" else "⚡",
                    style = TextStyle(fontSize = 24.sp)
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Column {
                    Text(
                        text = "$streak DAY STREAK",
                        style = TextStyle(
                            color = ColorProvider(if (streak > 0) accentAmber else textPrimary),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = if (streak > 0) "Active • Keep it burning" else "Complete an activity today",
                        style = TextStyle(
                            color = ColorProvider(textSecondary),
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(10.dp))

            // Quick Actions
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Add Note Button
                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .background(cardDark)
                        .cornerRadius(8.dp)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .clickable(actionStartActivity(addNoteIntent)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✍️ Add Note",
                        style = TextStyle(
                            color = ColorProvider(accentMint),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.width(8.dp))

                // Check Habit Button
                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .background(cardDark)
                        .cornerRadius(8.dp)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .clickable(actionStartActivity(checkHabitIntent)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓ Habit",
                        style = TextStyle(
                            color = ColorProvider(accentAmber),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
