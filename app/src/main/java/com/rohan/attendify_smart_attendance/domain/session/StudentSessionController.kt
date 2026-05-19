package com.rohan.attendify_smart_attendance.domain.session

import android.util.Log
import com.rohan.attendify_smart_attendance.data.ble.BleBroadcastClient

import com.rohan.attendify_smart_attendance.domain.repository.StudentSessionRepository
import kotlinx.coroutines.*

class StudentSessionController(
    private val bleBroadcast: BleBroadcastClient,
    private val sessionScope: CoroutineScope,
    private val studentRepo: StudentSessionRepository
) {

    private var currentStatus = StudentSessionState()
    private var sessionJob: Job? = null

    companion object {
        private const val BROADCAST_WINDOW = 3 * 60_000L
        private const val REST_WINDOW = 2 * 60_000L
        private const val MAX_SESSION_TIME = 90 * 60_000L
    }

    data class StudentSessionState(
        val isBroadcasting: Boolean = false,
        val startTime: Long = 0L,
        val errorMessage: String? = null,
    )

    private fun updateState() {
        studentRepo.updateStatus(currentStatus)
    }

    fun startAttendance(bleUuid: String) {

        if (bleUuid.isBlank()) {
            currentStatus = currentStatus.copy(errorMessage = "Invalid Student ID")
            updateState()
            return
        }

        // Prevent double starting
        if (currentStatus.isBroadcasting) return

        val startTime = System.currentTimeMillis()
        val endTime = startTime + MAX_SESSION_TIME


        currentStatus = currentStatus.copy(
            isBroadcasting = true,
            startTime = startTime,
            errorMessage = null,
        )
        updateState()

        sessionJob = sessionScope.launch {

            while (isActive && System.currentTimeMillis() < endTime) {
                // BROADCAST= 3 min
                startAdvertisingHelper(bleUuid)
                delay(BROADCAST_WINDOW)

                //  REST= 2 min
                if (System.currentTimeMillis() < endTime) {
                    delay(REST_WINDOW)
                }
            }
            stopAttendance()
        }
    }

    fun stopAttendance() {
        sessionJob?.cancel()
        sessionJob = null
        stopAdvertisingHelper()
    }


    private fun startAdvertisingHelper(bleUuid: String) {
        Log.d("StudentController", "Starting Broadcast ,Student ID : $bleUuid ")
        bleBroadcast.startAttendance(bleUuid)

        currentStatus = currentStatus.copy(
            isBroadcasting = true,
        )
        updateState()
    }

    private fun stopAdvertisingHelper() {
        Log.d("StudentController", "Session Stopped")
        bleBroadcast.stopAttendance()
        currentStatus = currentStatus.copy(
            isBroadcasting = false
        )
        updateState()
    }
}