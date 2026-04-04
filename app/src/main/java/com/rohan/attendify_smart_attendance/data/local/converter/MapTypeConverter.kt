package com.rohan.attendify_smart_attendance.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MapTypeConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromHitMap(hitMap: Map<String, Int>): String {
        return gson.toJson(hitMap)
    }

    @TypeConverter
    fun toHitMap(hitMapString: String): Map<String, Int> {
        val mapType = object : TypeToken<Map<String, Int>>() {}.type
        return gson.fromJson(hitMapString, mapType)
    }
}