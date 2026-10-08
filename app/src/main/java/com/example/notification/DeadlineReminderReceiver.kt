package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DeadlineReminderReceiver : BroadcastReceiver() {
    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, 0L)
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Task Deadline"

        NotificationHelper.showDeadlineNotification(
            context = context,
            taskId = taskId,
            taskTitle = taskTitle,
            subtitle = "Your deadline is due now"
        )
    }
}
