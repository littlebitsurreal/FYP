package com.example.skeleton

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import com.example.skeleton.helper.UsageStatsHelper
import java.util.Timer
import java.util.TimerTask
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.support.v4.app.NotificationCompat
import com.example.skeleton.client.UpdateServerTask
import com.example.skeleton.helper.CalendarHelper
import com.example.skeleton.helper.Logger
import com.example.skeleton.helper.NotTrackingListHelper
import com.example.skeleton.helper.NotificationHelper
import com.example.skeleton.helper.PackageHelper
import com.example.skeleton.helper.UsageStatsHelper.queryTodayUsage
import com.example.skeleton.model.UsageRecord
import java.util.concurrent.locks.ReentrantLock

/*
 * different method to handle this class for Android version < 26 and >= 26
 *
 * Android version < 26
 *      START_STICKY & restart using pending intent after service is killed
 *
 * Android version >= 26
 *      register for foreground service
 *      live-time register for Screen Unlock Receiver
 */

class MyService : Service() {
    private var mLastQueryTime: Long = 0
    private var mTimer: Timer? = null
    private var mTodayUsages = HashMap<String, Long>()
    private var mNotTrackingList = listOf<String>()
    private var mReminderOn = true
    private var mStrictMode = true
    private var mUsageLimit = 30 * 60 * 1000

    override fun onCreate() {
        super.onCreate()
        mNotTrackingList = NotTrackingListHelper.getNotTrackingList(this)
        mUsageLimit = getSharedPreferences("MyService", Context.MODE_PRIVATE).getInt("mUsageLimit", 30 * 60 * 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground()
            registerScreenUnlockReceiver()
        }
        loadUsages()
        startForegroundListener()
        Logger.d("MyService", "onCreate")
    }

    override fun onDestroy() {
        Logger.d("MyService", "onDestroy")
        mTimer?.cancel()
        sendBroadcast(Intent(this, ServiceEndReceiver::class.java))
        unregisterReceiver(ScreenUnlockReceiver())
        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder? {
        Logger.d("MyService", "onBind")
        return null
    }

    override fun onRebind(intent: Intent?) {
        Logger.d("MyService", "onRebind")
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Logger.d("MyService", "onUnbind")
        return super.onUnbind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Logger.d("MyService", "onTaskRemoved")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            val restartService = Intent(applicationContext,
                    this.javaClass)
            restartService.`package` = packageName
            val restartServicePI = PendingIntent.getService(
                    applicationContext, 1, restartService,
                    PendingIntent.FLAG_ONE_SHOT)
            val alarmService = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 3000, restartServicePI)
        }
        super.onTaskRemoved(rootIntent)
    }

    fun startForeground() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val icon = BitmapFactory.decodeResource(resources, R.drawable.elephant)

        val notification = NotificationCompat.Builder(this, "default")
                .setContentTitle(resources.getString(R.string.app_name))
                .setContentText("Tracking App Usage")
                .setLargeIcon(icon)
                .setSmallIcon(R.drawable.notification_template_icon_low_bg)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .build()
        startForeground(System.currentTimeMillis().toInt() % 10000, notification) //get a random id
    }

    fun stopForeground() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            stopForeground(true)
        }
    }
    private fun registerScreenUnlockReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction("android.intent.action.USER_PRESENT")
        registerReceiver(ScreenUnlockReceiver(), intentFilter)
    }
    // endregion

    // Events
    private fun startForegroundListener() {
        val timer = Timer(true)
        var lastForegroundPackageName: String? = null
        val usageStatsManager = getSystemService(AppCompatActivity.USAGE_STATS_SERVICE) as UsageStatsManager

        mTimer = timer
        mLastQueryTime = getSharedPreferences("MyService", Context.MODE_PRIVATE).getLong("mLastQueryTime", 1000L)

        val monitoringTask = object : TimerTask() {
            val lock = ReentrantLock()

            override fun run() {
                if (lock.tryLock()) {
                    val result = UsageStatsHelper.getLatestEvent(this@MyService, usageStatsManager, mLastQueryTime, System.currentTimeMillis())
                    val foregroundEvent = if (result.records.isEmpty()) {
                        null
                    } else {
                        result.records.last()
                    }

                    if (result.lastEndTime != 0L) {
                        mLastQueryTime = result.lastEndTime
                        val prefEdit = getSharedPreferences("MyService", Context.MODE_PRIVATE).edit()
                        prefEdit.putLong("mLastQueryTime", mLastQueryTime)
                        prefEdit.commit()
                    }

                    addUsages(result.records)

                    if (foregroundEvent != null) {
                        if (lastForegroundPackageName != foregroundEvent.packageName) {
                            onAppSwitch(foregroundEvent.packageName)
                        }
                        lastForegroundPackageName = foregroundEvent.packageName
                    }
                }
                lock.unlock()
            }
        }
        val updateServerTask = UpdateServerTask(this)
        timer.schedule(monitoringTask, 0, AppConfig.TIMER_CHECK_PERIOD)
        timer.schedule(updateServerTask, 10000, AppConfig.TIMER_UPDATE_SERVER_PERIOD)
    }

    private fun onAppSwitch(packageName: String) {
        val t = mTodayUsages[packageName] ?: 0
        Logger.d("onAppSwitch", "$packageName - usage: ${t
                / 60000}min  limit: ${mUsageLimit / 60000}  In NotTrackingList: ${mNotTrackingList.contains(packageName)}")

        if (t >= mUsageLimit && !mNotTrackingList.contains(packageName)) {
            NotificationHelper.show(this, "Usage Monitor",
                    "You have used ${PackageHelper.getAppName(this, packageName)} for ${CalendarHelper.toReadableDuration(t)}", packageName.hashCode())
        }
    }

    private fun loadUsages() {
        val records = queryTodayUsage(this)
        for (r in records) {
            mTodayUsages[r.packageName] = (mTodayUsages[r.packageName] ?: 0) + r.duration
        }
    }

    private fun addUsages(records: List<UsageRecord>) {
        for (r in records) {
            mTodayUsages[r.packageName] = (mTodayUsages[r.packageName] ?: 0) + r.duration
        }
    }
    //endregion
}
