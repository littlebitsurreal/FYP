package com.example.skeleton.helper

import android.app.usage.UsageEvents
import android.app.usage.UsageEvents.Event.MOVE_TO_BACKGROUND
import android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND
import android.app.usage.UsageStatsManager
import android.content.Context
import com.example.skeleton.model.UsageDigest
import com.example.skeleton.model.UsageRecord
import org.json.JSONObject
import java.io.File

object UsageStatsHelper {
    data class Result(val records: List<UsageRecord>, val lastEndTime: Long)
    const val HOUR_24 = 1000 * 60 * 60 * 24L
    private var mLastForegroundEvent: String? = null
    private var mLastTimeStamp: Long = 0

    fun getLatestEvent(context: Context, usageStatsManager: UsageStatsManager, startTime: Long, endTime: Long): Result {
        val records = arrayListOf<UsageRecord>()
        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        var lastEndTime: Long = 0

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            val packageName = event.packageName
            val timeStamp = event.timeStamp
            val eventType = event.eventType

            if (eventType == MOVE_TO_BACKGROUND && mLastForegroundEvent == packageName && timeStamp > mLastTimeStamp) {
                mLastForegroundEvent?.let {
                    recordUsage(context, it, mLastTimeStamp, timeStamp - mLastTimeStamp)
                }
                mLastForegroundEvent = null
                lastEndTime = timeStamp
            } else if (eventType == MOVE_TO_FOREGROUND) {
                mLastForegroundEvent = packageName
                mLastTimeStamp = timeStamp
                records.add(UsageRecord(packageName, timeStamp, timeStamp - mLastTimeStamp))
            }
        }
        return Result(records, lastEndTime)
    }

    private fun recordUsage(context: Context, packageName: String, startTime: Long, duration: Long) {
        Logger.d("recordUsage", "$packageName   from ${CalendarHelper.getDate(startTime)}  -  ${duration / 1000}s")
        val filename = CalendarHelper.getDayCondensed(startTime)
        val path = File(context.filesDir.path + "/" + filename)
        CsvHelper.write(path, listOf(UsageRecord(packageName, startTime, duration)))
    }

    fun queryIntervalUsage(context: Context, duration: Long): ArrayList<UsageRecord> {
        val currentTime = System.currentTimeMillis()
        val startTime = currentTime - duration
        val records = arrayListOf<UsageRecord>()

        for (i in startTime..currentTime step HOUR_24) {
            val filename = CalendarHelper.getDayCondensed(i)
            val file = File(context.filesDir.path + "/" + filename)
            records.addAll(CsvHelper.read(file))
        }
        return records
    }

    fun getTodayUsageTime(context: Context): Long {
        return queryTodayUsage(context).map { it.duration }.sum()
    }

    fun queryUsage(context: Context, day: String): List<UsageRecord> {
        val file = File(context.filesDir.path + "/" + day)
        return CsvHelper.read(file)
    }

    fun queryTodayUsage(context: Context): List<UsageRecord> {
        return queryUsage(context, CalendarHelper.getDayCondensed(System.currentTimeMillis()))
    }

    fun query24hUsage(context: Context): List<UsageRecord> {
        return queryIntervalUsage(context, HOUR_24).filter { it.starTime >= System.currentTimeMillis() - HOUR_24 }
    }

    fun getAverageUsageTime(context: Context): Long {
        val pref = context.getSharedPreferences(UsageDigest.TAG, Context.MODE_PRIVATE)
        val all = pref.all
        var sum = 0L
        for ((_, value) in all) {
            val t = UsageDigest.fromJson(JSONObject(value as? String)).totalTime
            sum += t
        }
        if (sum == 0L || all.isEmpty()) {
            return 0L
        } else {
            return (sum / all.size)
        }
    }
}
