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
    private val scope: CoroutineScope
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
    private val SERVICE_UUID = ParcelUuid(UUID.fromString("0000FFFF-0000-1000-8000-00805F9B34FB"))

    @SuppressLint("MissingPermission")
    fun startScanning() {
        if (bluetoothAdapter?.isEnabled == false) {
            Log.e("BleClient", "Bluetooth disabled. Cannot scan.")
            return
        }

        // Hardware Filtering
        val filter = ScanFilter.Builder()
            .setServiceData(SERVICE_UUID, null)
            .build()

        val filters = listOf(filter)

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

                val scanRecord = result.scanRecord ?: return

                val serviceData = scanRecord.getServiceData(SERVICE_UUID)

                if (serviceData != null) {
                    try {
                        //  Decode: Convert 16 bytes -> java.util.UUID
                        val buffer = java.nio.ByteBuffer.wrap(serviceData)
                        val mostSigBits = buffer.long
                        val leastSigBits = buffer.long
                        val studentUuid = UUID(mostSigBits, leastSigBits).toString().lowercase()

                        _scanResults.tryEmit(studentUuid)

                    } catch (e: Exception) {
                        Log.e("BleClient", "Failed to decode student ID: ${e.message}")
                    }
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e("BleClient", "Scan failed with error: $errorCode")
                handleScanFailure(errorCode)
            }
        }

        try {
            scanner?.startScan(filters, settings, scanCallback)
            restartAttempts = 0
        } catch (e: Exception) {
            Log.e("BleClient", " Exception starting scan: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        try {
            scanCallback?.let {
                scanner?.stopScan(it)
            }
            scanCallback = null

        } catch (e: Exception) {
            Log.e("BleClient", "Ignored stop error: ${e.message}")
        }
    }

    private fun handleScanFailure(errorCode: Int) {
        if (errorCode == ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED) {
            if (restartAttempts < MAX_RETRY_ATTEMPTS) {
                restartAttempts++

                scope.launch {
                    stopScanning()
                    delay(500)
                    startScanning()
                }
            } else {
                Log.e("BleClient", "Max retries reached. Giving up.")
            }
        }
    }
}