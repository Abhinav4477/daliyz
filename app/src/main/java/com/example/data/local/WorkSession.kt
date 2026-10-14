package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "work_sessions",
    indices = [Index(value = ["dateKey"]), Index(value = ["endTime"])]
)
data class WorkSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val durationMillis: Long,
    val dateKey: String, // YYYY-MM-DD
    val notes: String = ""
)
