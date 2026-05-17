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
        var hasPerms = true

        //Check Notification Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPerms = hasPerms && checkPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        }

        //  Check Bluetooth  Permissions
        hasPerms = hasPerms && if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkPermission(context, Manifest.permission.BLUETOOTH_SCAN) &&
                    checkPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE) &&
                    checkPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        }

        return hasPerms
    }

    fun requestBluetoothPermissions(activity: Activity, requestCode: Int) {
        val permissionsToRequest = mutableListOf<String>()

        //  Add Notification Permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        //  Add Bluetooth/Location Permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        ActivityCompat.requestPermissions(activity, permissionsToRequest.toTypedArray(), requestCode)
    }

    private fun checkPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}