package com.example.skeleton

import android.app.job.JobParameters
import android.app.job.JobService
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import com.example.skeleton.helper.UsageStatsHelper
import java.util.Timer
import java.util.TimerTask
import android.app.AlarmManager
import android.app.PendingIntent
import android.os.SystemClock
import com.example.skeleton.helper.CalendarHelper
import com.example.skeleton.helper.Logger
import com.example.skeleton.helper.UsageStatsHelper.HOUR_24
import com.example.skeleton.helper.UsageStatsHelper.queryTodayUsage
import com.example.skeleton.model.UsageDigest
import com.example.skeleton.model.UsageRecord
import java.util.concurrent.locks.ReentrantLock

class MyService : JobService() {
    private var mLastQueryTime: Long = 0
    private var mTimer: Timer? = null
    private var usages = HashMap<String, Long>()
    private var usageLimit = 1000 * 60 * 1

    // region Life cycle
    override fun onStartJob(params: JobParameters?): Boolean {
        Logger.d("MyService", "onStartJob")
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Logger.d("MyService", "onStopJob")
        return true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        loadUsages()
        startForegroundListener()

        Logger.d("MyService", "onCreate")
    }

    override fun onDestroy() {
        mTimer?.cancel()
        Logger.d("MyService", "onDestroy")
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartService = Intent(applicationContext,
                this.javaClass)
        restartService.`package` = packageName
        val restartServicePI = PendingIntent.getService(
                applicationContext, 1, restartService,
                PendingIntent.FLAG_ONE_SHOT)
        val alarmService = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 3000, restartServicePI)
        super.onTaskRemoved(rootIntent)
    }
    // endregion

    private fun startForegroundListener() {
        val timer = Timer(true)
        var lastForegroundPackageName: String? = null
        val usageStatsManager = getSystemService(AppCompatActivity.USAGE_STATS_SERVICE) as UsageStatsManager
        val prefEdit = getSharedPreferences("MyService", Context.MODE_PRIVATE).edit()
        val pref = getSharedPreferences("MyService", Context.MODE_PRIVATE)

        mTimer = timer
        mLastQueryTime = getSharedPreferences("MyService", Context.MODE_PRIVATE).getLong("mLastQueryTime", 0L)

        val monitoringTask = object : TimerTask() {
            val lock = ReentrantLock()

            override fun run() {
                if (lock.tryLock()) {
                    val currentTime = System.currentTimeMillis()
                    val records = UsageStatsHelper.getLatestEvent(this@MyService, usageStatsManager, mLastQueryTime, currentTime)
                    val foregroundEvent = if (records.isEmpty()) {
                        null
                    } else {
                        records.last()
                    }

                    mLastQueryTime = currentTime
                    prefEdit.putLong("mLastQueryTime", mLastQueryTime)
                    prefEdit.apply()

                    addUsages(records)

                    if (foregroundEvent != null) {
                        if (lastForegroundPackageName != foregroundEvent.packageName) {
                            notifyAppChange(foregroundEvent.packageName)
                        }
                        lastForegroundPackageName = foregroundEvent.packageName
                    }
                }
                lock.unlock()
            }
        }
        val updateServerTask = object : TimerTask() {
            override fun run() {
                val day = CalendarHelper.getDayCondensed(System.currentTimeMillis() - HOUR_24)
                val updatedDays = pref.getStringSet("updated", setOf<String>()).toMutableSet()
                if (!updatedDays.contains(day)) {
                    val digest = UsageDigest.load(this@MyService, day)
                    if (digest == null || digest.totalTime == 0L) {
                        updatedDays.add(day)
                        prefEdit.putStringSet("updated", updatedDays)
                        prefEdit.apply()
                        Logger.d("updateServerTask", "empty record")
                    } else {
                        // TODO: update server
                        var success = true
                        if (success) {
                            updatedDays.add(day)
                            prefEdit.putStringSet("updated", updatedDays)
                            prefEdit.apply()
                            Logger.d("updateServerTask", "success")
                        } else {
                            Logger.d("updateServerTask", "failed")
                        }
                    }
                }
                val digest = UsageDigest
            }
        }
        timer.schedule(monitoringTask, 0, AppConfig.TIMER_CHECK_PERIOD)
        timer.schedule(updateServerTask, 10000, 60 * 1000)
    }

    private fun notifyAppChange(packageName: String) {
        var reminderOn = true
        var strictMode = true
        var notTrackingList = listOf<String>()

        if (usages[packageName] ?: 0 >= usageLimit) {
            Logger.d("notifyAppChange", "$packageName exceed usage limit")
        }
    }

    private fun loadUsages() {
        val records = queryTodayUsage(this)
        for (r in records) {
            usages[r.packageName] = (usages[r.packageName] ?: 0) + r.duration
        }
    }

    private fun addUsages(records: List<UsageRecord>) {
        for (r in records) {
            usages[r.packageName] = (usages[r.packageName] ?: 0) + r.duration
        }
    }
}
