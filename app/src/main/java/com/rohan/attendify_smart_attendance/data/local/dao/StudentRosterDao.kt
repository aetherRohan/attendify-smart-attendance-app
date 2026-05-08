package com.rohan.attendify_smart_attendance.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rohan.attendify_smart_attendance.data.local.entity.StudentRosterEntity

@Dao
interface StudentRosterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoster(students: List<StudentRosterEntity>)

    @Query("SELECT * FROM student_roster WHERE classId = :classId")
    suspend fun getStudentsForClass(classId: String): List<StudentRosterEntity>

    @Query("DELETE FROM student_roster")
    suspend fun clearAllStudents()
}