package com.example.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class NoteWithTodos(
    @Embedded
    val note: WorkNote,
    @Relation(
        parentColumn = "id",
        entityColumn = "noteId"
    )
    val todos: List<NoteTodoItem> = emptyList()
)
