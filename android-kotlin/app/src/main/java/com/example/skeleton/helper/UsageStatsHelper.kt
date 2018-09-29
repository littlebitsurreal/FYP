package com.example.skeleton.helper

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlin.math.min

object UsageStatsHelper {
    fun getForegroundEvent(context: Context, usageStatsManager: UsageStatsManager): UsageEvents.Event? {
        return null
    }
}
