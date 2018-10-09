package com.example.skeleton.helper

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object CalanderHelper {
    fun getDate(time: Long): String {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = time
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(cal.time)
    }
    fun getDateCondensed(time: Long): String {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = time
        return SimpleDateFormat("yyyyMMddHHmmssSSS").format(cal.time)
    }
    fun getDayCondensed(time: Long): String {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = time
        return SimpleDateFormat("yyyyMMdd").format(cal.time)
    }
}
