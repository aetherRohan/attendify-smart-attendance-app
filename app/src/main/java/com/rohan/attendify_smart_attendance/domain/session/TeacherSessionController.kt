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
    private val teacherRepository: TeacherSessionRepository
) {
    private lateinit var classId: String
    private var currentWindowIndex: Int = 1
    private var currentStatus= SessionStatus()
    private val dataMutex = Mutex()
    private val studentsInClass= mutableMapOf<String, String>()
    private val studentIdNameInCurrentWindow = mutableMapOf<String, String>()
    private val studentsNamesInCurrentWindow = HashSet<String>()
    private val studentsIdsInCurrentWindow = HashSet<String>()


    private var microCycleCounter = 0
    private var sessionStartTime: Long = 0


    private fun updateState() {
        teacherRepository.updateStatus(currentStatus)
    }

    // 1.for now fetch the students from server and store them locally  x/ x
    // 2.create temp db of class session  ,need current date xx/xx
    // 3.and inc window hit count for every 5 min window  xx/xx
    // 4. after stoping the session impl the coroutine worker for data sync with server

    fun startSession(id: String) {
        classId=id
        if (currentStatus.isRunning || classId.isBlank()) return

        Log.i(TAG, "Starting Session: 5-min Windows, One-Hit Threshold")
        sessionStartTime = System.currentTimeMillis()

        // Reset local state
        microCycleCounter = 0
        studentsNamesInCurrentWindow.clear()

        currentStatus=currentStatus.copy(
            isRunning = true,
            studentsFoundCount = studentsNamesInCurrentWindow.size,
            studentList=  studentsNamesInCurrentWindow.toList()
        )
        updateState()

        //fetch the student from server and store ble uuid in a hashset
        sessionScope.launch {

            //for demo it fetches the student roster everytime the session starts
            teacherRepository.fetchAndSaveRoster(classId)
            Log.i("session","saved the student from db locally")

            val studentList = teacherRepository.getStudentsForClass(classId)
            Log.i("size"," the size of the list is :${studentList.size}")

            studentList.forEach { student ->
                studentsInClass.put(student.bleUuid,student.name)
            }
            Log.i("session","saved the ble ids in hashSet")
            while (isActive) {
                runOneMicroCycle()
            }
        }
    }


    fun stopSession(classId: String) {
        if (!currentStatus.isRunning) return

        val durationMin = (System.currentTimeMillis() - sessionStartTime) / 60000


       currentStatus=currentStatus.copy(
           isRunning = false
       )
        updateState()
        bleClient.stopScanning()
        sessionScope.cancel()
//        sessionScope = null

        // Add to Coroutine for syncing with server when internet is available

        runBlocking {
            addToPendingSession(classId, durationMin, currentWindowIndex)
        }
    }

    private suspend fun runOneMicroCycle() {
        // --- PHASE 1: SCAN & COLLECT ---
        val collectionJob = bleClient.scanResults.onEach { studentId ->
            dataMutex.withLock {
                // If student is in the class and  If new for this 5-min window, add them.
                val studentName = studentsInClass[studentId]

                if (studentName != null && !studentIdNameInCurrentWindow.contains(studentId)) {

                    studentIdNameInCurrentWindow.put(studentId, studentName)
                    studentsNamesInCurrentWindow.add(studentName)
                    studentsIdsInCurrentWindow.add(studentId)


                    currentStatus = currentStatus.copy(
                        studentsFoundCount = studentsNamesInCurrentWindow.size,
                        studentList = studentsNamesInCurrentWindow.sorted()
                    )
                    updateState()

                    Log.e("StudentId", studentId)
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
            syncWindowData(currentWindowIndex, studentsIdsInCurrentWindow.sorted())

            // Reset for next 5-min block
            microCycleCounter = 0
            dataMutex.withLock {
                studentsNamesInCurrentWindow.clear()
                studentIdNameInCurrentWindow.clear()
                studentsIdsInCurrentWindow.clear()
            }
            currentWindowIndex = currentWindowIndex + 1
        }
        //REST ---
        delay(REST_DURATION)
    }

    //  call the local db and inc the window hit
    private fun syncWindowData(index: Int, students: List<String>) {

        sessionScope.launch {
            Log.i(TAG, "Syncing WINDOW #$index to local database | Students found: ${students.size}")

            try {
                teacherRepository.recordCurrentWindowAttendance(
                    classId = classId,
                    windowIndex = index,
                    scannedStudents = students
                )

                Log.i(TAG, "Window #$index successfully saved to db")

            } catch (e: Exception) {

                Log.e(TAG, "CRITICAL: Failed to sync window #$index data: ${e.message}", e)
            }
        }
    }

    // call the local db for the last time and inc window hit count
    // stop the session and add to coroutine work Manager for data sync when internet available
    private  fun addToPendingSession(id: String, duration: Long, windows: Int) {
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