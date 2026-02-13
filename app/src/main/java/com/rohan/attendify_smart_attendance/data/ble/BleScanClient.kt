package com.rohan.attendify_smart_attendance.data.ble
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.os.ParcelUuid
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.util.UUID

class BleScanClient(
    private val bluetoothAdapter: BluetoothAdapter?,
    private val scope: CoroutineScope // Pass a scope (e.g., lifecycleScope or explicit)
) {
    private val scanner: BluetoothLeScanner?
        get() = bluetoothAdapter?.bluetoothLeScanner

    private val _scanResults = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 64
    )
    val scanResults: SharedFlow<String> = _scanResults

    private var scanCallback: ScanCallback? = null

    private var restartAttempts = 0
    private val MAX_RETRY_ATTEMPTS = 4

    // UUID of  the CLASS
    private val SERVICE_UUID = UUID.fromString("0000180A-0000-1000-8000-00805F9B34FB")

    @SuppressLint("MissingPermission")
    fun startScanning() {
        if (bluetoothAdapter?.isEnabled == false) {
            Log.e("BleClient", "Bluetooth disabled. Cannot scan.")
            return
        }

        // Hardware Filtering
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()


        //EMPTY LIST FOR TESTING
        val filters = listOf<ScanFilter>()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .build()

        //  Prevent Duplicate Callbacks
        if (scanCallback != null) {
            stopScanning()
        }

        scanCallback = object : ScanCallback() {

            override fun onScanResult(callbackType: Int, result: ScanResult) {
                // 1. Get the Scan Record (The payload)
                val scanRecord = result.scanRecord ?: return

                //DEMO
                val device = result.device
                val name = scanRecord.deviceName ?: device.name ?: "Unknown"
                _scanResults.tryEmit(name)
//               Log.e("DeviceName",name)

                val serviceData = scanRecord.getServiceData(ParcelUuid(SERVICE_UUID))
                // 3. Validation: Check if data exists and is not empty
                if (serviceData != null && serviceData.isNotEmpty()) {
                    try {
                        // 4. Decode: Convert bytes -> String
                        val studentId = String(
                            serviceData,
                            Charsets.UTF_8
                        ).trim() // trim() removes accidental spaces
                        Log.d(
                            "BleClient",
                            "✅ Found Student ID: $studentId (Signal: ${result.rssi} dBm)"
                        )
                        // 5. Emit to UI
                        _scanResults.tryEmit(studentId)
                    } catch (e: Exception) {
                        Log.e("BleClient", "Failed to decode student ID: ${e.message}")
                    }
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e("BleClient", "❌ Scan failed with error: $errorCode")
                handleScanFailure(errorCode)
            }
        }

        try {
            scanner?.startScan(filters, settings, scanCallback)
            Log.d("BleClient", "Scan Started cleanly")
            restartAttempts = 0 // Reset success
        } catch (e: Exception) {
            Log.e("BleClient", "Native Exception starting scan: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        try {
            scanCallback?.let {
                scanner?.stopScan(it)
            }
            scanCallback = null
            Log.d("BleClient", "Scan Stopped.")
        } catch (e: Exception) {
            // Common Android Bug: "BluetoothAdapter is turned off" throws here
            Log.w("BleClient", "Ignored stop error: ${e.message}")
        }
    }

    private fun handleScanFailure(errorCode: Int) {
        if (errorCode == ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED) {
            if (restartAttempts < MAX_RETRY_ATTEMPTS) {
                restartAttempts++
                Log.w("BleClient", "Attempting recovery #$restartAttempts...")

                // 3. Enterprise Safety: Add Delay before retry
                scope.launch {
                    stopScanning()
                    delay(500) // Give the hardware stack 500ms to breathe
                    startScanning()
                }
            } else {
                Log.e("BleClient", "Critical: Max retries reached. Giving up.")
                // In a real app, emit a "SystemError" state to UI here
            }
        }
    }
}