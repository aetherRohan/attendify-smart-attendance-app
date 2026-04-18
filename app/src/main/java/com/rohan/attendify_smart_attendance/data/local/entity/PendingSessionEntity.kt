package com.rohan.attendify_smart_attendance.data.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rohan.attendify_smart_attendance.dto.SessionSyncRequest
import java.sql.Date

@Entity(tableName = "pending_sessions")
data class PendingSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val localSessionId: Long = 0,
    val classId: String,
    val sessionStartDate: String,
    val totalWindows: Int,
    val studentHitsMap: Map<String, Int>
)

