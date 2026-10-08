package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Priority {
    LOW,
    NORMAL,
    HIGH
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val dueDateMillis: Long? = null,
    val priority: Priority = Priority.NORMAL,
    val isCompleted: Boolean = false,
    val hasReminder: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
