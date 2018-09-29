package com.example.skeleton

import android.app.job.JobParameters
import android.app.job.JobService
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.example.skeleton.helper.UsageStatsHelper
import java.util.Timer
import java.util.TimerTask

class MyService : JobService() {
    // region Life cycle
    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d("MyService", "onStartJob")
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d("MyService", "onStopJob")
        return true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("MyService", "onStartCommand")
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundListener()
        Log.d("MyService", "onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MyService", "onDestroy")
    }
    // endregion

    private fun startForegroundListener() {
        val timer = Timer(true)
        var lastForegroundPackageName: String? = null
        val usageStatsManager = getSystemService(AppCompatActivity.USAGE_STATS_SERVICE) as UsageStatsManager
        val timerTask = object : TimerTask() {
            override fun run() {
                val event = UsageStatsHelper.getForegroundEvent(this@MyService, usageStatsManager)
                if (event != null && event.packageName != null) {
                    if (lastForegroundPackageName != event.packageName) {
                        notifyAppChange(event.packageName)
                    }
                    lastForegroundPackageName = event.packageName
                }
            }
        }
        timer.schedule(timerTask, 0, AppConfig.TIMER_CHECK_PERIOD)
    }

    private fun notifyAppChange(name: String) {
}
