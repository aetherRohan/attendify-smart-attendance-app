package com.rohan.attendify_smart_attendance.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import java.nio.ByteBuffer
import java.util.UUID

class BleBroadcastClient(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        manager.adapter
    }

    private val advertiser: BluetoothLeAdvertiser?
        get() = bluetoothAdapter?.bluetoothLeAdvertiser

    private var advertiseCallback: AdvertiseCallback? = null
    private val SERVICE_UUID = ParcelUuid(UUID.fromString("0000FFFF-0000-1000-8000-00805F9B34FB"))


    @SuppressLint("MissingPermission")
    fun startAttendance(bleUuid: String): Boolean {
        if (advertiser == null) {
            Log.e("BleAdvertiser", "❌ CRITICAL: Device does not support Bluetooth LE Advertising.")
            return false
        }

        // Prevent duplicate broadcasts
        if (advertiseCallback != null) {
            Log.w("BleAdvertiser", "⚠️ Advertising already active. Stopping previous session first.")
            stopAttendance()
        }

        try {
            // Convert Student UUID String -> 16 raw bytes
            val uuid = UUID.fromString(bleUuid)
            val dataBytes = ByteBuffer.allocate(16).apply {
                putLong(uuid.mostSignificantBits)
                putLong(uuid.leastSignificantBits)
            }.array()

            //  Settings
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(false) //  We are broadcasting ONLY. No connections allowed.
                .setTimeout(0)
                .build()

            // Data: Pack the 0xFFFF Key + 16-byte Student ID
            val data = AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .addServiceUuid(SERVICE_UUID) // "Key" (0xFFFF)
                .addServiceData(SERVICE_UUID, dataBytes) // "Payload" (Ble Uuid)
                .build()

            // Callback
            advertiseCallback = object : AdvertiseCallback() {
                override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                    Log.i("BleAdvertiser", "✅ Broadcast STARTED. ID: $bleUuid")
                    Log.v("BleAdvertiser", "TxPower: ${settingsInEffect.txPowerLevel}, Mode: ${settingsInEffect.mode}")
                }

                override fun onStartFailure(errorCode: Int) {
                    val errorMsg = when (errorCode) {
                        ADVERTISE_FAILED_DATA_TOO_LARGE -> "Data too large"
                        ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> "Feature unsupported"
                        ADVERTISE_FAILED_INTERNAL_ERROR -> "Internal error"
                        ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> "Too many advertisers"
                        else -> "Unknown error ($errorCode)"
                    }
                    Log.e("BleAdvertiser", "❌ Broadcast FAILED: $errorMsg")
                }
            }

            //  Start Broadcasting Student ID
            advertiser?.startAdvertising(settings, data, advertiseCallback)
            return true

        } catch (e: Exception) {
            Log.e("BleAdvertiser", "❌ Exception starting advertising: ${e.message}")
            return false
        }
    }


    @SuppressLint("MissingPermission")
    fun stopAttendance(){
        if (advertiser == null) return

        try {
            advertiseCallback?.let {
                advertiser?.stopAdvertising(it)
                Log.d("BleAdvertiser", "🛑 Broadcast STOPPED successfully.")
            }
            advertiseCallback = null
        } catch (e: Exception) {
            Log.e("BleAdvertiser", "⚠️ Error stopping advertising: ${e.message}")
        }
    }
}