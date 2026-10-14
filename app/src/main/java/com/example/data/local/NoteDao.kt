package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Transaction
    @Query("SELECT * FROM work_notes ORDER BY updatedAt DESC")
    fun getAllNotesWithTodos(): Flow<List<NoteWithTodos>>

    @Transaction
    @Query("SELECT * FROM work_notes WHERE id = :id")
    fun getNoteWithTodosById(id: Long): Flow<NoteWithTodos?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: WorkNote): Long

    @Update
    suspend fun updateNote(note: WorkNote)

    @Query("DELETE FROM work_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: NoteTodoItem): Long

    @Update
    suspend fun updateTodo(todo: NoteTodoItem)

    @Query("DELETE FROM note_todos WHERE id = :id")
    suspend fun deleteTodoById(id: Long)

    @Query("DELETE FROM note_todos WHERE noteId = :noteId")
    suspend fun deleteTodosForNote(noteId: Long)
}
