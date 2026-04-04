package com.rohan.attendify_smart_attendance.data.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_sessions")
data class PendingSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val localSessionId: Long = 0,

    val classId: String,
    val sessionStartTime: Long,
    val totalWindows: Int,

    // This is where your MapTypeConverter kicks in!
    // It stores: { "ble-uuid-1": 4, "ble-uuid-2": 5 }
    val studentHitsMap: Map<String, Int>
)