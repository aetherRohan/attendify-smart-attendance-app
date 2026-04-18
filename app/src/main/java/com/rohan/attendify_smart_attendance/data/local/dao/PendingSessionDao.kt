package com.rohan.attendify_smart_attendance.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rohan.attendify_smart_attendance.data.local.entity.PendingSessionEntity

@Dao
interface PendingSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingSession(session: PendingSessionEntity): Long


    @Query("SELECT * FROM pending_sessions")
    suspend fun getAllPendingSessions(): List<PendingSessionEntity>


    @Query("DELETE FROM pending_sessions WHERE localSessionId = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("SELECT * FROM pending_sessions WHERE classId = :classId AND sessionStartDate = :date LIMIT 1")
    suspend fun getSessionForToday(classId: String, date: String): PendingSessionEntity?

    @Update
    suspend fun updateSession(session: PendingSessionEntity)


    @Query("DELETE FROM pending_sessions")
    suspend fun clearAllPendingSessions()
}