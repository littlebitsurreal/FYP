package com.example.skeleton.helper

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object CalendarHelper {
    fun getDate(time: Long): String {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = time
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(cal.time)
    }
    fun getDayCondensed(time: Long): String {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = time
        return SimpleDateFormat("yyyyMMdd").format(cal.time)
    }
    fun getTimeConsensed(time: Long): String {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = time
        return SimpleDateFormat("HH:mm").format(cal.time)
    }
    fun getDay(time: Long): String {
        return DateFormat.getDateInstance(DateFormat.LONG).format(time)
    }
    fun getMonthDay(time: Long): String {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = time
        return SimpleDateFormat("dd/MM").format(cal.time)
    }
    fun toReadableDuration(duration: Long): String {
        val t = duration / 1000
        return if (t < 60) t.toString() + " sec" else (t / 60).toString() + " min"
    }
}
