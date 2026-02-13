package com.rohan.attendify_smart_attendance.domain.session

import android.util.Log
import com.rohan.attendify_smart_attendance.data.ble.BleScanClient
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


object TeacherSessionController {

    private const val TAG = "SessionController"
    private const val SCAN_DURATION = 30000L // 30s
    private const val REST_DURATION = 30000L // 30s
    private const val CYCLES_PER_WINDOW = 5  // 5 Minutes Total
    private var bleClient: BleScanClient? = null
    private var sessionScope: CoroutineScope? = null

    // --- STATE MANAGEMENT ---
    data class SessionStatus(
        val isRunning: Boolean = false,
        val currentWindowIndex: Int = 1,
        val studentsFoundCount: Int = 0,
        val studentList: List<String> = emptyList()
    )

    private val _status = MutableStateFlow(SessionStatus())
    val status = _status.asStateFlow()

    private val dataMutex = Mutex()
    private val studentsInCurrentWindow = HashSet<String>()

    private var microCycleCounter = 0
    private var sessionStartTime: Long = 0

    fun initialize(client: BleScanClient) {
        this.bleClient = client
    }


    fun startSession() {
        if (_status.value.isRunning) return

        Log.i(TAG, "Starting Session: 5-min Windows, One-Hit Threshold")
        sessionStartTime = System.currentTimeMillis()

        // Reset local state
        microCycleCounter = 0
        studentsInCurrentWindow.clear()
        _status.value = SessionStatus(isRunning = true, studentList = studentsInCurrentWindow.toList())

        sessionScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        sessionScope?.launch {
            while (isActive) {
                runOneMicroCycle()
            }
        }
    }


    fun stopSession(classId: String) {
        if (!_status.value.isRunning) return

        val durationMin = (System.currentTimeMillis() - sessionStartTime) / 60000
        val finalWindowCount = _status.value.currentWindowIndex - 1

        sessionScope?.cancel()
        sessionScope = null
        bleClient?.stopScanning()

        _status.value = SessionStatus(isRunning = false)

        // SEND FINAL SUMMARY
        runBlocking {
            uploadClassSummary(classId, durationMin, finalWindowCount)
        }
    }


     // Core Loop: 1 Minute (30s Scan + 30s Rest)

    private suspend fun runOneMicroCycle() {
        // --- PHASE 1: SCAN & COLLECT ---
        val collectionJob = bleClient?.scanResults?.onEach { studentId ->
            dataMutex.withLock {
                // ONE-HIT LOGIC: If new for this 5-min window, add them.
                if (studentsInCurrentWindow.add(studentId)) {
                    _status.value = _status.value.copy(
                        studentsFoundCount = studentsInCurrentWindow.size,
                        studentList = studentsInCurrentWindow.sorted()
                    )
                    Log.e("DeviceName",studentId)

                }
            }
        }?.launchIn(sessionScope!!)

        bleClient?.startScanning()
        delay(SCAN_DURATION)
        bleClient?.stopScanning()

        // Stop the collector until the next 30s scan starts
        collectionJob?.cancel()

        // --- PHASE 2: WINDOW MANAGEMENT ---
        microCycleCounter++

        if (microCycleCounter >= CYCLES_PER_WINDOW) {
            // 5 Minutes reached. Send window report.
            uploadWindowData(_status.value.currentWindowIndex, studentsInCurrentWindow.toList())

            // Reset for next 5-min block
            microCycleCounter = 0
            dataMutex.withLock { studentsInCurrentWindow.clear() }

            _status.value = _status.value.copy(
                currentWindowIndex = _status.value.currentWindowIndex + 1
            )
        }
        // --- PHASE 3: REST ---
        delay(REST_DURATION)
    }

    // --- API MOCKS (Use Retrofit in Production) ---

    private fun uploadWindowData(index: Int, students: List<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.i(TAG, "UPLOADING WINDOW #$index | Students: ${students.size}")
            // api.post("/attendance/window", { windowIndex: index, presentList: students })
        }
    }

    private suspend fun uploadClassSummary(id: String, duration: Long, windows: Int) {
        Log.i(TAG, "UPLOADING SUMMARY | Duration: $duration min | Windows: $windows")

        // api.post("/attendance/end", { classId: id, totalDuration: duration, totalWindows: windows })
        Thread.sleep(1000)
    }
}