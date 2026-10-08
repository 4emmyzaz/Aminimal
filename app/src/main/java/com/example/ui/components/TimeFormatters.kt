package com.example.ui.components

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object TimeFormatters {
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

    fun formatDeadline(epochMillis: Long): String {
        val now = Calendar.getInstance()
        val due = Calendar.getInstance().apply { timeInMillis = epochMillis }

        val isToday = now.get(Calendar.YEAR) == due.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == due.get(Calendar.DAY_OF_YEAR)

        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val isTomorrow = tomorrow.get(Calendar.YEAR) == due.get(Calendar.YEAR) &&
                tomorrow.get(Calendar.DAY_OF_YEAR) == due.get(Calendar.DAY_OF_YEAR)

        return when {
            isToday -> "Today, ${timeFormat.format(Date(epochMillis))}"
            isTomorrow -> "Tomorrow, ${timeFormat.format(Date(epochMillis))}"
            now.get(Calendar.YEAR) == due.get(Calendar.YEAR) ->
                SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(epochMillis))
            else -> dateTimeFormat.format(Date(epochMillis))
        }
    }

    fun getDeadlineRelativeStatus(epochMillis: Long): DeadlineStatus {
        val now = System.currentTimeMillis()
        val diff = epochMillis - now

        return when {
            diff < 0 -> {
                val absDiff = -diff
                val minutes = TimeUnit.MILLISECONDS.toMinutes(absDiff)
                val hours = TimeUnit.MILLISECONDS.toHours(absDiff)
                val days = TimeUnit.MILLISECONDS.toDays(absDiff)

                val label = when {
                    minutes < 60 -> "Overdue by ${minutes}m"
                    hours < 24 -> "Overdue by ${hours}h"
                    else -> "Overdue by ${days}d"
                }
                DeadlineStatus(label = label, isOverdue = true, isUrgent = true)
            }
            diff <= TimeUnit.HOURS.toMillis(2) -> {
                val minutes = (diff / (1000 * 60)).coerceAtLeast(1)
                DeadlineStatus(label = "Due in ${minutes}m", isOverdue = false, isUrgent = true)
            }
            diff <= TimeUnit.HOURS.toMillis(24) -> {
                val hours = (diff / (1000 * 60 * 60)).coerceAtLeast(1)
                DeadlineStatus(label = "Due in ${hours}h", isOverdue = false, isUrgent = false)
            }
            else -> {
                DeadlineStatus(label = formatDeadline(epochMillis), isOverdue = false, isUrgent = false)
            }
        }
    }

    fun formatNoteDate(epochMillis: Long): String {
        return dateFormat.format(Date(epochMillis))
    }
}

data class DeadlineStatus(
    val label: String,
    val isOverdue: Boolean,
    val isUrgent: Boolean
)
