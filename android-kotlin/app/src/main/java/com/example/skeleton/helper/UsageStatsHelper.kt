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
    const val HOUR_24 = 1000 * 60 * 60 * 24L
    private var mLastForegroundEvent: String? = null
    private var mLastTimeStamp: Long = 0

    fun getLatestEvent(context: Context, usageStatsManager: UsageStatsManager, startTime: Long, endTime: Long): List<UsageRecord> {
        val records = arrayListOf<UsageRecord>()
        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()

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
            } else if (eventType == MOVE_TO_FOREGROUND) {
                mLastForegroundEvent = packageName
                mLastTimeStamp = timeStamp
                records.add(UsageRecord(packageName, timeStamp, timeStamp - mLastTimeStamp))
            }
        }
        return records
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

    fun queryNDayBeforeUsage(context: Context, n: Int): List<UsageRecord> {
        val time = System.currentTimeMillis() - HOUR_24 * n
        val filename = CalendarHelper.getDayCondensed(time)
        val file = File(context.filesDir.path + "/" + filename)
        return CsvHelper.read(file)
    }

    fun queryTodayUsage(context: Context): List<UsageRecord> {
        return queryNDayBeforeUsage(context, 0)
    }

    fun query24hUsage(context: Context): List<UsageRecord> {
        val time = System.currentTimeMillis() - HOUR_24
        return queryIntervalUsage(context, HOUR_24).filter { it.starTime >= time }
    }

    fun query7dayUsage(context: Context): List<UsageRecord> {
        return queryIntervalUsage(context, HOUR_24 * 6)
    }

    fun query30dayUsage(context: Context): List<UsageRecord> {
        return queryIntervalUsage(context, HOUR_24 * 29)
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
            return 60 * 60 * 1000
        } else {
            return (sum / all.size)
        }
    }
}
