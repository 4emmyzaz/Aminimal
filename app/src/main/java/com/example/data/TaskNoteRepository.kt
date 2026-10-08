package com.example.data

import kotlinx.coroutines.flow.Flow

class TaskNoteRepository(
    private val taskDao: TaskDao,
    private val noteDao: NoteDao
) {
    // Tasks
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    fun getPendingDeadlines(now: Long = System.currentTimeMillis()): Flow<List<TaskEntity>> {
        return taskDao.getPendingDeadlines(now)
    }

    suspend fun getTaskById(id: Long): TaskEntity? = taskDao.getTaskById(id)

    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    suspend fun deleteTaskById(id: Long) = taskDao.deleteTaskById(id)

    suspend fun setTaskCompleted(id: Long, completed: Boolean) =
        taskDao.updateTaskCompleted(id, completed)

    // Notes
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()

    suspend fun getNoteById(id: Long): NoteEntity? = noteDao.getNoteById(id)

    suspend fun insertNote(note: NoteEntity): Long = noteDao.insertNote(note)

    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)

    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)

    suspend fun deleteNoteById(id: Long) = noteDao.deleteNoteById(id)

    suspend fun toggleNotePin(id: Long, isPinned: Boolean) =
        noteDao.togglePin(id, isPinned, System.currentTimeMillis())
}
