package com.rohan.attendify_smart_attendance.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionManager {

    fun hasBluetoothPermissions(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ (API 31+) logic
            checkPermission(context, Manifest.permission.BLUETOOTH_SCAN) &&
                    checkPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE) &&
                    checkPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            // Android 11 & Older logic (Location is required for scanning)
            checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun requestBluetoothPermissions(activity: Activity, requestCode: Int) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        ActivityCompat.requestPermissions(activity, permissions, requestCode)
    }

    private fun checkPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}