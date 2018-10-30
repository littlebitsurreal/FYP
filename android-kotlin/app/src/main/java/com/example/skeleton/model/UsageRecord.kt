package com.example.skeleton.model

import com.example.skeleton.helper.CalendarHelper

data class UsageRecord(
        val packageName: String = "",
        val starTime: Long = 0,
        val duration: Long = 0
) {
    fun toArray(): Array<String> {
        return arrayOf(packageName, starTime.toString(), duration.toString())
    }

    override fun toString(): String {
        return "packageName: $packageName  startTime: ${CalendarHelper.getDate(starTime)}  duration: ${duration / 1000}s"
    }
}
