package com.example.skeleton

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import com.example.skeleton.helper.UsageStatsHelper
import java.util.Timer
import java.util.TimerTask
import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.Binder
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
import com.example.skeleton.redux.ViewStore
import org.json.JSONObject
import java.lang.ref.WeakReference
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
    class MyBinder(myService: MyService) : Binder() {
        val service = WeakReference(myService)
    }

    private val binder = MyBinder(this)
    private var mLastQueryTime: Long = 0
    private var mTimer: Timer? = null
    private var mTodayUsages = HashMap<String, Long>()
    private var mNotTrackingList: List<String>? = null
    private var mToday: String = ""
    var isReminderOn: Boolean = false
    var isStrictModeOn: Boolean = false
    var usageLimit: Int = 30

    //region Life cycle
    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground()
            registerScreenUnlockReceiver()
        } else {
            try {
                if (JSONObject(getSharedPreferences("redux", Context.MODE_PRIVATE).getString("view", "")).getBoolean("isForegroundOn")) {
                    startForeground()
                }
            } catch (e: Exception) {
            }
        }

        loadRedux()
        loadUsages()
        loadNotTrackingList()

        startForegroundListener()
        Logger.d("MyService", "onCreate")
    }

    override fun onDestroy() {
        Logger.d("MyService", "onDestroy")
        mTimer?.cancel()
        sendBroadcast(Intent(this, ServiceEndReceiver::class.java))
        try {
            unregisterReceiver(ScreenUnlockReceiver())
        } catch (e: Exception) {
        }

        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder? {
        Logger.d("MyService", "onBind")
        return binder
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
                .setSmallIcon(R.drawable.ic_timeline)
                .setVisibility(Notification.VISIBILITY_SECRET)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .build()
        startForeground(50, notification) //get a random id
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
                    val result = UsageStatsHelper.getLatestEvent(usageStatsManager, mLastQueryTime, System.currentTimeMillis())
                    val foregroundEvent = result.foregroundPackageName

                    if (result.lastEndTime != 0L) {
                        mLastQueryTime = result.lastEndTime
                        val prefEdit = getSharedPreferences("MyService", Context.MODE_PRIVATE).edit()
                        prefEdit.putLong("mLastQueryTime", mLastQueryTime)
                        prefEdit.commit()
                    }

                    addUsages(result.records)
                    if (foregroundEvent != null && !foregroundEvent.contains("skeleton")) {
                        if (lastForegroundPackageName != foregroundEvent) {
                            onAppSwitch(foregroundEvent)
                        }
                        lastForegroundPackageName = foregroundEvent
                    }
                }
                lock.unlock()
            }
        }
        val updateServerTask = UpdateServerTask(this)
        timer.schedule(monitoringTask, 0, AppConfig.TIMER_CHECK_PERIOD)
        timer.schedule(updateServerTask, 30000, AppConfig.TIMER_UPDATE_SERVER_PERIOD)
    }

    private fun onAppSwitch(packageName: String) {
        val t = mTodayUsages[packageName] ?: 0
        Logger.d("onAppSwitch", "$packageName - usage: ${t
                / 60000}min  limit: $usageLimit  In NotTrackingList: ${mNotTrackingList?.contains(packageName)}")

        if (t >= usageLimit * 60000 && mNotTrackingList?.contains(packageName) == false) {
            if (isReminderOn) {
                // TODO: add this app into exeption
                if (!isStrictModeOn) {
                    NotificationHelper.show(
                            this,
                            "${PackageHelper.getAppName(this, packageName)} - ${CalendarHelper.toReadableDuration(t)}",
                                    "Why not take a break?",
//                            packageName.hashCode()
                            1
                    )
                } else {
                    val intent = Intent(this, BlockerActivity::class.java)
                    intent.flags = FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            }
        }
    }

    private fun loadRedux() {
        val pref = getSharedPreferences("redux", Context.MODE_PRIVATE)
        val store = try {
            ViewStore.load(JSONObject(pref.getString("view", ""))) ?: return
        } catch (e: Exception) {
            Logger.e("loadRedux", "${e.message}, use default value")
            ViewStore.State()
        }
        isReminderOn = store.isReminderOn
        isStrictModeOn = store.isStrictModeOn
        usageLimit = store.usageLimit
    }

    fun loadNotTrackingList() {
        mNotTrackingList = NotTrackingListHelper.loadNotTrackingList(this)
    }

    private fun loadUsages() {
        val records = queryTodayUsage()
        mTodayUsages = HashMap()
        mToday = CalendarHelper.getDate(System.currentTimeMillis())
        for (r in records) {
            mTodayUsages[r.packageName] = (mTodayUsages[r.packageName] ?: 0) + r.duration
        }
    }

    private fun addUsages(records: List<UsageRecord>) {
        if (records.isNotEmpty() && CalendarHelper.getDate(records.first().starTime) != mToday) {
            loadUsages()
        }
        for (r in records) {
            mTodayUsages[r.packageName] = (mTodayUsages[r.packageName] ?: 0) + r.duration
        }
    }
    //endregion
}
