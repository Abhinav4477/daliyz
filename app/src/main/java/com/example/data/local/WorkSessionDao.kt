package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkSession): Long

    @Query("SELECT * FROM work_sessions ORDER BY endTime DESC")
    fun getAllSessions(): Flow<List<WorkSession>>

    @Query("SELECT * FROM work_sessions WHERE dateKey = :dateKey ORDER BY endTime DESC")
    fun getSessionsForDate(dateKey: String): Flow<List<WorkSession>>

    @Query("SELECT * FROM work_sessions WHERE startTime >= :startMillis AND endTime <= :endMillis ORDER BY startTime ASC")
    fun getSessionsBetween(startMillis: Long, endMillis: Long): Flow<List<WorkSession>>

    @Query("SELECT SUM(durationMillis) FROM work_sessions WHERE dateKey = :dateKey")
    fun getTodayTotalDuration(dateKey: String): Flow<Long?>

    @Query("DELETE FROM work_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    /**
     * Permanently purges all work history older than the specified cutoff timestamp (e.g. 90 days / 3 months).
     */
    @Query("DELETE FROM work_sessions WHERE endTime < :cutoffTimestamp")
    suspend fun deleteSessionsOlderThan(cutoffTimestamp: Long): Int

    @Query("SELECT COUNT(*) FROM work_sessions")
    suspend fun getTotalSessionCount(): Int
}
