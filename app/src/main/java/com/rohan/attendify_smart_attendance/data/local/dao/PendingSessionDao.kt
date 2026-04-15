package com.rohan.attendify_smart_attendance.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rohan.attendify_smart_attendance.data.local.entity.PendingSessionEntity

@Dao
interface PendingSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingSession(session: PendingSessionEntity): Long

    // Your WorkManager will call this when internet connects
    @Query("SELECT * FROM pending_sessions")
    suspend fun getAllPendingSessions(): List<PendingSessionEntity>

    // Call this ONLY after Retrofit gets a successful 200 OK from Spring Boot
    @Query("DELETE FROM pending_sessions WHERE localSessionId = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("SELECT * FROM pending_sessions WHERE classId = :classId AND sessionStartDate = :date LIMIT 1")
    suspend fun getSessionForToday(classId: String, date: String): PendingSessionEntity?

    @Update
    suspend fun updateSession(session: PendingSessionEntity)
}