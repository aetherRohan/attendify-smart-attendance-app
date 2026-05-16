package com.rohan.attendify_smart_attendance.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rohan.attendify_smart_attendance.data.local.entity.ClassSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClassSession(classSession: List<ClassSessionEntity>)

    @Query("SELECT * FROM class_sessions WHERE classId=:classId ")
     fun getAllClassSessions(classId: String): Flow<List<ClassSessionEntity>>

    @Query("DELETE FROM class_sessions")
    suspend fun clearAllClassSessions()

}