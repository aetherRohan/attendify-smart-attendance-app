package com.rohan.attendify_smart_attendance.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rohan.attendify_smart_attendance.data.local.converter.MapTypeConverter
import com.rohan.attendify_smart_attendance.data.local.dao.AttendanceDao
import com.rohan.attendify_smart_attendance.data.local.dao.ClassDao
import com.rohan.attendify_smart_attendance.data.local.dao.ClassSessionDao
import com.rohan.attendify_smart_attendance.data.local.dao.PendingSessionDao
import com.rohan.attendify_smart_attendance.data.local.dao.StudentRosterDao
import com.rohan.attendify_smart_attendance.data.local.entity.AttendanceEntity
import com.rohan.attendify_smart_attendance.data.local.entity.ClassEntity
import com.rohan.attendify_smart_attendance.data.local.entity.ClassSessionEntity
import com.rohan.attendify_smart_attendance.data.local.entity.PendingSessionEntity
import com.rohan.attendify_smart_attendance.data.local.entity.StudentRosterEntity

@Database(
    entities = [StudentRosterEntity::class, PendingSessionEntity::class,
                 ClassEntity::class, ClassSessionEntity::class, AttendanceEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(MapTypeConverter::class)
abstract class AttendifyDatabase : RoomDatabase() {

    abstract fun studentRosterDao(): StudentRosterDao
    abstract fun pendingSessionDao(): PendingSessionDao
    abstract fun classDao(): ClassDao

    abstract fun attendanceDao(): AttendanceDao
    abstract fun classSessionDao(): ClassSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AttendifyDatabase? = null

        fun getDatabase(context: Context): AttendifyDatabase {
            // If the database already exists, return it.
            // If it doesn't exist, lock the thread and build it safely.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AttendifyDatabase::class.java,
                    "attendify_offline_database" // file name on the phone
                )
                    .fallbackToDestructiveMigration(dropAllTables = false)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}