package com.rohan.attendify_smart_attendance.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rohan.attendify_smart_attendance.data.local.entity.ClassEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassDao {

    @Query("SELECT * FROM classes")
     fun getAllClassesFlow(): Flow<List<ClassEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertClasses(classes: List<ClassEntity>)

    @Query("DELETE FROM classes")
     fun clearAllClasses()

}