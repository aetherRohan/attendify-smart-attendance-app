package com.rohan.attendify_smart_attendance.domain.session

import android.util.Log
import com.rohan.attendify_smart_attendance.data.ble.BleScanClient
import com.rohan.attendify_smart_attendance.repository.AttendanceRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
class TeacherSessionController {

    private var bleClient: BleScanClient? = null
    private var sessionScope: CoroutineScope? = null

    private var currentWindowIndex: Int = 0
    private var isRunning: Boolean = false


    private val dataMutex = Mutex()
    private val studentsInCurrentWindow = HashSet<String>()

    private var microCycleCounter = 0
    private var sessionStartTime: Long = 0

    fun initialize(client: BleScanClient,sessionScope: CoroutineScope) {
        this.bleClient = client
        this.sessionScope=sessionScope
    }

    private fun updateState(isRunning: Boolean, count: Int, list: List<String>) {
        val newStatus = SessionStatus(
            isRunning = isRunning,
            studentsFoundCount = count,
            studentList = list
        )
        AttendanceRepository.updateStatus(newStatus)
    }

    fun startSession() {
        if (isRunning) return

        Log.i(TAG, "Starting Session: 5-min Windows, One-Hit Threshold")
        sessionStartTime = System.currentTimeMillis()

        // Reset local state
        microCycleCounter = 0
        studentsInCurrentWindow.clear()

        updateState(
            isRunning = true,
            count = studentsInCurrentWindow.size,
           list =  studentsInCurrentWindow.toList()
        )


        sessionScope?.launch {
            while (isActive) {
                runOneMicroCycle()
            }
        }
    }


    fun stopSession(classId: String) {
        if (isRunning) return

        val durationMin = (System.currentTimeMillis() - sessionStartTime) / 60000
        val finalWindowCount = if (currentWindowIndex>1) currentWindowIndex - 1 else currentWindowIndex

        sessionScope?.cancel()
        sessionScope = null
        bleClient?.stopScanning()

        updateState(
            isRunning = false,
            count = studentsInCurrentWindow.size,
            list =  studentsInCurrentWindow.sorted()
        )

        // SEND FINAL SUMMARY
        runBlocking {
            uploadClassSummary(classId, durationMin, finalWindowCount)
        }
    }


    private suspend fun runOneMicroCycle() {
        // --- PHASE 1: SCAN & COLLECT ---
        val collectionJob = bleClient?.scanResults?.onEach { studentId ->
            dataMutex.withLock {
                // ONE-HIT LOGIC: If new for this 5-min window, add them.
                if (studentsInCurrentWindow.add(studentId)) {
                    updateState(
                        isRunning = true,
                        count = studentsInCurrentWindow.size,
                        list =  studentsInCurrentWindow.sorted()
                    )
                    Log.e("DeviceName",studentId)
                }
            }
        }?.launchIn(sessionScope!!)

        bleClient?.startScanning()
        delay(SCAN_DURATION)
        bleClient?.stopScanning()
        collectionJob?.cancel()

        // WINDOW MANAGEMENT ---
        microCycleCounter++

        if (microCycleCounter >= CYCLES_PER_WINDOW) {
            // 5 Minutes reached ,send window report
            uploadWindowData(currentWindowIndex, studentsInCurrentWindow.sorted())

            // Reset for next 5-min block
            microCycleCounter = 0
            dataMutex.withLock { studentsInCurrentWindow.clear() }
                currentWindowIndex =currentWindowIndex + 1
        }
        //REST ---
        delay(REST_DURATION)
    }


    private fun uploadWindowData(index: Int, students: List<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.i(TAG, "UPLOADING WINDOW #$index | Students: ${students.size}")
            // api.post("/attendance/window", { windowIndex: index, presentList: students })
        }
    }

    private  fun uploadClassSummary(id: String, duration: Long, windows: Int) {
        Log.i(TAG, "UPLOADING SUMMARY | Duration: $duration min | Windows: $windows")

        // api.post("/attendance/end", { classId: id, totalDuration: duration, totalWindows: windows })
        // demo
        Thread.sleep(1000)
    }


    companion object {
        private const val TAG = "SessionController"
        // Time constants are clearer when grouped here
        private const val SCAN_DURATION = 30_000L
        private const val REST_DURATION = 30_000L
        private const val CYCLES_PER_WINDOW = 5
    }

    data class SessionStatus(
        val isRunning: Boolean = false,
        val studentsFoundCount: Int = 0,
        val studentList: List<String> = emptyList()
    )
}