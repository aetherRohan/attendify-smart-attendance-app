package com.rohan.attendify_smart_attendance.domain.session

import com.rohan.attendify_smart_attendance.data.ble.BleBroadcastClient
import com.rohan.attendify_smart_attendance.entity.ConnectionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update



class StudentSessionController{

    private var serviceScope : CoroutineScope?=null
    private  var bleBroadcast: BleBroadcastClient?=null


    private val BROADCAST_WINDOW = 3 * 60 * 1000L // 3 Minutes Broadcasting
    private val REST_WINDOW = 2 * 60 * 1000L      // 2 Minutes Resting
    private val MAX_CLASS_DURATION = 90 * 60 * 1000L // 90 Minutes (Safety Cut-off)

    data class StudentSessionState(
        val isBroadcasting: Boolean = false,
        val studentId: String? = null,
        val classId: String? = null,
        val startTime: Long = 0L,
        val errorMessage: String? = null,
        val connectionStatus: ConnectionStatus = ConnectionStatus.IDLE
    )

    fun initialize(bleBroadcast: BleBroadcastClient,scope: CoroutineScope){
        this.bleBroadcast=bleBroadcast
        this.serviceScope=scope
    }



    private val _state = MutableStateFlow(StudentSessionState())
    val state: StateFlow<StudentSessionState> = _state.asStateFlow()

    private var timerJob: Job? = null
    // MATCH THIS WITH YOUR SERVICE: 90 Minutes Safety Stop
    private val MAX_SESSION_TIME = 90 * 60 * 1000L





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
        bleBroadcast?.startAttendance(studentId)




    }

    /**
     * Call this when the student swipes "Leave Class"
     */
    fun stopAttendance() {

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
        bleBroadcast?.stopAttendance()
    }



}