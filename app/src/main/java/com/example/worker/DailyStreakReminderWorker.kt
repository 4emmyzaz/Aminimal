package com.example.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.notification.NotificationHelper
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/**
 * Daily WorkManager task that fires around 8:00 PM.
 * Checks if the user has completed a habit or note today. If not, sends a high-priority
 * push notification specifically designed to protect their streak before midnight.
 */
class DailyStreakReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val db = AppDatabase.getDatabase(context)
        val todayStr = LocalDate.now().toString()

        val activitiesToday = db.activityLogDao().countActivitiesOnDate(todayStr)
        if (activitiesToday == 0) {
            // User hasn't completed any habit or note today!
            val streakState = db.streakDao().getStreakState()
            val currentStreak = streakState?.currentStreak ?: 0
            val freezes = streakState?.streakFreezesCount ?: 0

            showStreakProtectionNotification(context, currentStreak, freezes)
        }

        return Result.success()
    }

    private fun showStreakProtectionNotification(
        context: Context,
        currentStreak: Int,
        freezes: Int
    ) {
        NotificationHelper.createNotificationChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            STREAK_NOTIFICATION_ID,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (currentStreak > 0) {
            "Protect your $currentStreak-day streak! 🔥"
        } else {
            "Ignite your streak today! ⚡"
        }

        val contentText = if (currentStreak > 0) {
            if (freezes > 0) {
                "You haven't checked in yet today. Log a habit or note by midnight, or 1 freeze ($freezes remaining) will be consumed!"
            } else {
                "Warning: 0 freezes remaining! Complete a habit or note before midnight to avoid resetting your streak!"
            }
        } else {
            "Complete a habit or jot down a note before midnight to start your streak."
        }

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(STREAK_NOTIFICATION_ID, notification)
    }

    companion object {
        private const val STREAK_NOTIFICATION_ID = 8888
        private const val WORK_TAG_DAILY_STREAK = "daily_8pm_streak_reminder"

        /**
         * Schedules periodic daily execution at 8:00 PM.
         */
        fun schedule(context: Context) {
            val now = LocalDateTime.now()
            val targetToday = LocalDateTime.of(LocalDate.now(), LocalTime.of(20, 0))
            val initialDelayMinutes = if (now.isBefore(targetToday)) {
                Duration.between(now, targetToday).toMinutes()
            } else {
                Duration.between(now, targetToday.plusDays(1)).toMinutes()
            }

            val workRequest = PeriodicWorkRequestBuilder<DailyStreakReminderWorker>(
                24, TimeUnit.HOURS
            )
                .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
                .addTag(WORK_TAG_DAILY_STREAK)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_TAG_DAILY_STREAK,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
            Log.d("DailyStreakWorker", "Scheduled daily 8 PM reminder with initial delay: $initialDelayMinutes mins")
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_TAG_DAILY_STREAK)
        }
    }
}
