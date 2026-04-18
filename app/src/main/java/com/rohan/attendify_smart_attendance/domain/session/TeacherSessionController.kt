package com.rohan.attendify_smart_attendance.domain.session

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.rohan.attendify_smart_attendance.data.ble.BleScanClient
import com.rohan.attendify_smart_attendance.repository.TeacherSessionRepository
import com.rohan.attendify_smart_attendance.worker.SyncSessionWorker
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TeacherSessionController(
    val context: Context,
    private val bleClient: BleScanClient,
    private var sessionScope: CoroutineScope,
    private val teacherRepository: TeacherSessionRepository
) {
    private lateinit var classId: String
    private var currentWindowIndex: Int = 1
    private var currentStatus = SessionStatus()
    private val dataMutex = Mutex()
    private val studentsInClass = mutableMapOf<String, String>()
    private val studentIdNameInCurrentWindow = mutableMapOf<String, String>()
    private val studentsNamesInCurrentWindow = HashSet<String>()
    private val studentsIdsInCurrentWindow = HashSet<String>()


    private var microCycleCounter = 0
    private var sessionStartTime: Long = 0


    private fun updateState() {
        teacherRepository.updateStatus(currentStatus)
    }

    fun startSession(id: String) {
        classId = "1"
        if (currentStatus.isRunning || classId.isBlank()) return

        Log.i(TAG, "Starting Session: 5-min Windows, One-Hit Threshold")
        sessionStartTime = System.currentTimeMillis()

        // Reset local state
        microCycleCounter = 0
        studentsNamesInCurrentWindow.clear()

        currentStatus = currentStatus.copy(
            isRunning = true,
            studentsFoundCount = studentsNamesInCurrentWindow.size,
            studentList = studentsNamesInCurrentWindow.toList()
        )
        updateState()

        //fetch the student from server and store ble uuids in a hashset
        sessionScope.launch {

            //for demo it fetches the student roster everytime the session starts
            teacherRepository.fetchAndSaveRoster(classId)

            val studentList = teacherRepository.getStudentsForClass(classId)

            studentList.forEach { student ->
                studentsInClass.put(student.bleUuid, student.name)
            }

            while (isActive) {
                runOneMicroCycle()
            }
        }
    }


    fun stopSession() {
        if (!currentStatus.isRunning) return
        val durationMin = (System.currentTimeMillis() - sessionStartTime) / 60000

        currentStatus = currentStatus.copy(
            isRunning = false
        )
        updateState()
        bleClient.stopScanning()

        val finalIndex = currentWindowIndex
        val finalStudents = studentsIdsInCurrentWindow.sorted()
        val currentClassId = classId

        sessionScope.cancel()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                addToPendingSession(
                    index = finalIndex, students = finalStudents, classId = currentClassId
                )
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("worker", "${e.message}")
            }

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
            syncWindowData(currentWindowIndex, studentsIdsInCurrentWindow.sorted(),classId)

            // Reset for next 5-min block
            microCycleCounter = 0
            dataMutex.withLock {
                studentsNamesInCurrentWindow.clear()
                studentIdNameInCurrentWindow.clear()
                studentsIdsInCurrentWindow.clear()
            }
            currentWindowIndex = currentWindowIndex + 1
        }
        //REST
        delay(REST_DURATION)
    }

    //  call the local db and inc the window hit
    private fun syncWindowData(index: Int, students: List<String>,currentClassId: String) {

        sessionScope.launch {
            Log.i(
                TAG,
                "Syncing WINDOW #$index to local database | Students found: ${students.size}"
            )

            try {
                teacherRepository.recordCurrentWindowAttendance(
                    classId = currentClassId, windowIndex = index, scannedStudents = students
                )

                Log.i(TAG, "Window #$index successfully saved to db")

            } catch (e: Exception) {

                Log.e(TAG, "CRITICAL: Failed to sync window #$index data: ${e.message}", e)
            }
        }
    }

    private fun addToPendingSession(index: Int, students: List<String>,classId: String) {

        syncWindowData(index = index, students = students,classId)
        Log.i(TAG, "FINAL SYNC WITH DB DONE before adding to coroutine worker")


        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncSessionWorker>()
            .setConstraints(constraints)
            .build()


        WorkManager.getInstance(context).enqueueUniqueWork(
            "Offline_Attendance_Sync",
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            syncWorkRequest
        )
        Log.i("worker","added to pending session offline sync")
    }


    companion object {
        private const val TAG = "SessionController"
        private const val SCAN_DURATION = 30_000L
        private const val REST_DURATION = 30_000L
        private const val CYCLES_PER_WINDOW = 1
    }
    data class SessionStatus(
        val isRunning: Boolean = false,
        val studentsFoundCount: Int = 0,
        val studentList: List<String> = emptyList()
    )
}