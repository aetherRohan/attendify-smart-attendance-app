package com.rohan.attendify_smart_attendance.domain.session

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rohan.attendify_smart_attendance.entity.ConnectionStatus
import com.rohan.attendify_smart_attendance.service.StudentBroadcastService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// --- STATE CLASSES (Kept inside the same file for simplicity) ---

data class StudentSessionState(
    val isBroadcasting: Boolean = false,
    val studentId: String? = null,
    val classId: String? = null,
    val startTime: Long = 0L,
    val errorMessage: String? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.IDLE
)



// --- THE CONTROLLER ---

class StudentSessionController(
    application: Application
) : AndroidViewModel(application) {

    // 1. Reactive State (UI observes this)
    private val _state = MutableStateFlow(StudentSessionState())
    val state: StateFlow<StudentSessionState> = _state.asStateFlow()

    private var timerJob: Job? = null
    // MATCH THIS WITH YOUR SERVICE: 90 Minutes Safety Stop
    private val MAX_SESSION_TIME = 90 * 60 * 1000L

    /**
     * Call this when the student swipes "Start Class"
     */
    fun startAttendance(classId: String, studentId: String) {
        if (studentId.isBlank()) {
            _state.update { it.copy(errorMessage = "Invalid Student ID") }
            return
        }

        // 1. Update State immediately for UI responsiveness
        val currentTime = System.currentTimeMillis()
        _state.update {
            it.copy(
                isBroadcasting = true,
                classId = classId,
                studentId = studentId,
                startTime = currentTime,
                connectionStatus = ConnectionStatus.BROADCASTING,
                errorMessage = null
            )
        }

        // 2. Start the Foreground Service (The heavy lifter)
        startBroadcastService(studentId)

        // 3. Start Local UI Timer (Mirrors the service logic for visual feedback)
        startSessionTimer()
    }

    /**
     * Call this when the student swipes "Leave Class"
     */
    fun stopAttendance() {
        // 1. Kill the Service
        stopBroadcastService()

        // 2. Reset State
        _state.update {
            it.copy(
                isBroadcasting = false,
                connectionStatus = ConnectionStatus.IDLE,
                startTime = 0L
            )
        }

        // 3. Clean up
        timerJob?.cancel()
    }

    private fun startBroadcastService(studentId: String) {
        val context = getApplication<Application>()
        val intent = Intent(context, StudentBroadcastService::class.java).apply {
            action = StudentBroadcastService.ACTION_START_BROADCAST
            putExtra(StudentBroadcastService.EXTRA_STUDENT_ID, studentId)
        }

        // Android 8.0+ (Oreo) requires startForegroundService
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun stopBroadcastService() {
        val context = getApplication<Application>()
        val intent = Intent(context, StudentBroadcastService::class.java).apply {
            action = StudentBroadcastService.ACTION_STOP_BROADCAST
        }
        context.startService(intent)
    }

    // --- INTERNAL LOGIC: Cycle Tracking ---
    // This runs purely on the UI thread to update the "Status Text"
    // It estimates the 3min/2min cycle so the user knows what's happening.
    private fun startSessionTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()

            while (isActive) {
                val elapsed = System.currentTimeMillis() - startTime

                // Safety Auto-Stop (Mirrors Service)
                if (elapsed > MAX_SESSION_TIME) {
                    stopAttendance()
                    _state.update { it.copy(errorMessage = "Session timed out automatically.") }
                    break
                }

                // Cycle Logic: 5 min total (3 min ON, 2 min OFF)
                val cyclePosition = elapsed % (5 * 60 * 1000)
                val isResting = cyclePosition > (3 * 60 * 1000) // After 3rd minute, we rest

                _state.update {
                    it.copy(
                        connectionStatus = if (isResting) ConnectionStatus.RESTING else ConnectionStatus.BROADCASTING
                    )
                }

                delay(1000L) // Update every second
            }
        }
    }
}