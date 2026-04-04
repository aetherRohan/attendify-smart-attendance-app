package com.rohan.attendify_smart_attendance.domain.session

import android.util.Log
import com.rohan.attendify_smart_attendance.data.ble.BleScanClient
import com.rohan.attendify_smart_attendance.repository.TeacherSessionRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
class TeacherSessionController(
    private val bleClient: BleScanClient,
    private var sessionScope: CoroutineScope,
    private val teacherRepository: TeacherSessionRepository?
) {
    private var currentWindowIndex: Int = 0

    private var currentStatus= SessionStatus()

    private val dataMutex = Mutex()
    private val studentsInCurrentWindow = HashSet<String>()

    private var microCycleCounter = 0
    private var sessionStartTime: Long = 0


    private fun updateState() {
        teacherRepository?.updateStatus(currentStatus)
    }

    // 1.for now fetch the students from server and store them locally
    // 2.create temp db of class session  ,need current date
    // 3.and inc window hit count for every 5 min window
    // 4. after stoping the session impl the coroutine worker for data sync with server

    fun startSession(classId: String) {
        if (currentStatus.isRunning) return

        Log.i(TAG, "Starting Session: 5-min Windows, One-Hit Threshold")
        sessionStartTime = System.currentTimeMillis()

        // Reset local state
        microCycleCounter = 0
        studentsInCurrentWindow.clear()

        currentStatus=currentStatus.copy(
            isRunning = true,
            studentsFoundCount = studentsInCurrentWindow.size,
            studentList=  studentsInCurrentWindow.toList()
        )
        updateState()
        sessionScope.launch {

        }
        sessionScope.launch {
            while (isActive) {
                runOneMicroCycle()
            }
        }
    }


    fun stopSession(classId: String) {
        if (!currentStatus.isRunning) return

        val durationMin = (System.currentTimeMillis() - sessionStartTime) / 60000
        val finalWindowCount = if (currentWindowIndex>1) currentWindowIndex - 1 else currentWindowIndex

       currentStatus=currentStatus.copy(
           isRunning = false
       )
        updateState()
        bleClient.stopScanning()
        sessionScope.cancel()
//        sessionScope = null

        // SEND FINAL SUMMARY
        runBlocking {
            uploadClassSummary(classId, durationMin, finalWindowCount)
        }
    }

    // 5.Get the students from the db and store them in Hash set
    // 6.Only add the students in currentWindowStudentList if they are present in that list

    private suspend fun runOneMicroCycle() {
        // --- PHASE 1: SCAN & COLLECT ---
        val collectionJob = bleClient.scanResults.onEach { studentId ->
            dataMutex.withLock {
                // ONE-HIT LOGIC: If new for this 5-min window, add them.
                if (studentsInCurrentWindow.add(studentId)) {
                   currentStatus=currentStatus.copy(
                       studentsFoundCount=studentsInCurrentWindow.size,
                       studentList=studentsInCurrentWindow.sorted()
                   )
                    updateState()

                    Log.e("DeviceName",studentId)
                }
            }
        }.launchIn(sessionScope)

        bleClient.startScanning()
        delay(SCAN_DURATION)
        bleClient.stopScanning()
        collectionJob.cancel()

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

    //  call the local db and inc the window hit
    private fun uploadWindowData(index: Int, students: List<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.i(TAG, "UPLOADING WINDOW #$index | Students: ${students.size}")
            // api.post("/attendance/window", { windowIndex: index, presentList: students })
        }
    }

    // call the local db for the last time and inc window hit count
    // stop the session and add to coroutine work Manager for data sync when internet available
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