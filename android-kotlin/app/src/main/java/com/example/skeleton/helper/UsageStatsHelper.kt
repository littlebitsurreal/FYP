package com.example.skeleton.helper

import android.app.usage.UsageEvents
import android.app.usage.UsageEvents.Event.MOVE_TO_BACKGROUND
import android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.util.Log
import java.io.File

@Suppress("LiftReturnOrAssignment")
object UsageStatsHelper {
    const val HOUR_24 = 1000 * 60 * 60 * 24L
    private var mLastQueryTime: Long = 0
    private var mLastForegroundEvent: String? = null
    private var mLastTimeStamp: Long = 0

    fun getForegroundEvent(context: Context, usageStatsManager: UsageStatsManager): UsageEvents.Event? {
        val currentTime = System.currentTimeMillis()
        val usageEvents = usageStatsManager.queryEvents(if (mLastQueryTime == 0L) currentTime - 5 * 1000 else mLastQueryTime, currentTime)
        val event = UsageEvents.Event()
        var foregroundEvent: UsageEvents.Event? = null
        mLastQueryTime = currentTime

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            Log.i("getForegroundEvent", "${event.packageName}  timeStamp: ${CalanderHelper.getDate(event.timeStamp)}  type: ${event.eventType}")

            if (event.eventType == MOVE_TO_BACKGROUND && mLastForegroundEvent == event.packageName) {
                mLastForegroundEvent?.let {
                    recordUsage(context, it, mLastTimeStamp, event.timeStamp - mLastTimeStamp)
                }
                mLastForegroundEvent = null
            } else if (event.eventType == MOVE_TO_FOREGROUND) {
                mLastForegroundEvent = event.packageName
                mLastTimeStamp = event.timeStamp
                foregroundEvent = event
            }
        }
        return foregroundEvent
    }

    fun recordUsage(context: Context, packageName: String, startTime: Long, duration: Long) {
        val filename = CalanderHelper.getDayCondensed(System.currentTimeMillis())
        val path = File(context.filesDir.path + "/" + filename)
        CsvHelper.write(path, listOf(CsvHelper.UsageRecord(packageName, startTime, duration)))
    }

    fun queryUsage(context: Context, interval: Long) {
        val currentTime = System.currentTimeMillis()
        val startTime = currentTime - interval
        val records = arrayListOf<CsvHelper.UsageRecord>()

        for (i in startTime..currentTime step HOUR_24) {
            val filename = CalanderHelper.getDayCondensed(i)
            val file = File(context.filesDir.path + "/" + filename)
            records.addAll(CsvHelper.read(file))
        }

        Log.i("queryUsage", "today usage - ${records.sumBy { it.duration.toInt() } / 1000} seconds")
    }

    fun queryAllUsage(context: Context, interval: Long = HOUR_24 * 10): List<UsageStats> {
        val currentTime = System.currentTimeMillis()
        val usageStatsManager = context.getSystemService(AppCompatActivity.USAGE_STATS_SERVICE) as UsageStatsManager
        val usageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - interval, currentTime)

        return usageStats.sortedByDescending { it.totalTimeInForeground }
    }
}
