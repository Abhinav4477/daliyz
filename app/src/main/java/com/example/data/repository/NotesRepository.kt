package com.example.data.repository

import com.example.data.local.NoteDao
import com.example.data.local.NoteTodoItem
import com.example.data.local.NoteWithTodos
import com.example.data.local.WorkNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class NotesRepository(private val noteDao: NoteDao) {

    val allNotesWithTodos: Flow<List<NoteWithTodos>> = noteDao.getAllNotesWithTodos()
        .flowOn(Dispatchers.IO)

    fun getNoteWithTodosById(id: Long): Flow<NoteWithTodos?> {
        return noteDao.getNoteWithTodosById(id)
            .flowOn(Dispatchers.IO)
    }

    suspend fun createNote(
        title: String,
        content: String = "",
        csvNumbers: String = "",
        todoTexts: List<String> = emptyList()
    ): Long {
        return withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val note = WorkNote(
                title = title.trim().ifEmpty { "Untitled Note" },
                content = content,
                csvNumbers = csvNumbers,
                createdAt = now,
                updatedAt = now
            )
            val noteId = noteDao.insertNote(note)
            todoTexts.forEach { text ->
                if (text.isNotBlank()) {
                    noteDao.insertTodo(
                        NoteTodoItem(
                            noteId = noteId,
                            text = text.trim(),
                            isCompleted = false
                        )
                    )
                }
            }
            noteId
        }
    }

    suspend fun updateNote(
        id: Long,
        title: String,
        content: String,
        csvNumbers: String
    ) {
        withContext(Dispatchers.IO) {
            val updated = WorkNote(
                id = id,
                title = title.trim().ifEmpty { "Untitled Note" },
                content = content,
                csvNumbers = csvNumbers,
                updatedAt = System.currentTimeMillis()
            )
            noteDao.updateNote(updated)
        }
    }

    suspend fun deleteNote(id: Long) {
        withContext(Dispatchers.IO) {
            noteDao.deleteNoteById(id)
        }
    }

    suspend fun addTodoItem(noteId: Long, text: String): Long {
        return withContext(Dispatchers.IO) {
            if (text.isBlank()) return@withContext -1L
            noteDao.insertTodo(
                NoteTodoItem(
                    noteId = noteId,
                    text = text.trim(),
                    isCompleted = false
                )
            )
        }
    }

    suspend fun toggleTodoItem(todo: NoteTodoItem) {
        withContext(Dispatchers.IO) {
            noteDao.updateTodo(todo.copy(isCompleted = !todo.isCompleted))
        }
    }

    suspend fun deleteTodoItem(id: Long) {
        withContext(Dispatchers.IO) {
            noteDao.deleteTodoById(id)
        }
    }
}
