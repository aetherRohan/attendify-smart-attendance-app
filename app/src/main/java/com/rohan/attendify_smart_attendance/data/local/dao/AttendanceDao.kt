package com.rohan.attendify_smart_attendance.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rohan.attendify_smart_attendance.data.local.entity.AttendanceEntity
import com.rohan.attendify_smart_attendance.data.local.entity.ClassSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(classSession: List<AttendanceEntity>)

    @Query("SELECT * FROM attendance WHERE classSessionId=:classSessionId ")
     fun getAllAttendances(classSessionId: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE classSessionId=:classSessionId AND studentId=:studentId ")
     fun getAttendance(classSessionId: String,studentId: String): Flow<List<AttendanceEntity>>

    @Query("DELETE FROM attendance")
    suspend fun clearAllAttendances()

}