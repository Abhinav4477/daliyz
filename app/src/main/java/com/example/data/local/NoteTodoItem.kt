package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "note_todos",
    foreignKeys = [
        ForeignKey(
            entity = WorkNote::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["noteId"])]
)
data class NoteTodoItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noteId: Long,
    val text: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
