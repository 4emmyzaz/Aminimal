package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String = "",
    val isPinned: Boolean = false,
    val tag: String = "General",
    val updatedAt: Long = System.currentTimeMillis()
)
