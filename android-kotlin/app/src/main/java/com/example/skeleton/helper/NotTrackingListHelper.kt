package com.example.skeleton.helper

import android.content.Context
import com.example.skeleton.model.NotTrackingRecord
import com.example.skeleton.model.UsageSummary

object NotTrackingListHelper {
    fun getNotTrackingRecords(context: Context): List<NotTrackingRecord> {
        val pref = context.getSharedPreferences("NotTrackingListHelper", Context.MODE_PRIVATE)
        val list = pref.all
        // key = package name
        // value = app name
        return list.map { NotTrackingRecord(it.value.toString(), it.key, true) }
    }

    fun getNotTrackingSet(context: Context): List<String> {
        val pref = context.getSharedPreferences("NotTrackingListHelper", Context.MODE_PRIVATE)
        val list = pref.all
        return list.keys.toList()
    }

    fun addRecords(context: Context, l: List<NotTrackingRecord>) {
        Logger.d("NotTrackingListHelper", "add ${l.map { it.packageName }}")
        val pref = context.getSharedPreferences("NotTrackingListHelper", Context.MODE_PRIVATE).edit()
        for (r in l) {
            pref.putString(r.packageName, r.appName)
        }
        pref.apply()
    }

    fun removeRecords(context: Context, l: List<NotTrackingRecord>) {
        Logger.d("NotTrackingListHelper", "remove ${l.map { it.packageName }}")
        val pref = context.getSharedPreferences("NotTrackingListHelper", Context.MODE_PRIVATE).edit()
        for (r in l) {
            pref.remove(r.packageName)
        }
        pref.apply()
    }

    fun getList(context: Context): List<NotTrackingRecord> {
        val time = System.currentTimeMillis()
        val summary = UsageSummary.getSummary(context,
                ((time - 2 * UsageStatsHelper.HOUR_24)..time step UsageStatsHelper.HOUR_24).map { CalendarHelper.getDayCondensed(it) }
        )
        val trackingList = getNotTrackingRecords(context)
        val hashMap = hashMapOf<String, NotTrackingRecord>()
        hashMap.putAll(summary.map { Pair(it.packageName, NotTrackingRecord(it.appName, it.packageName, false)) })
        hashMap.putAll(trackingList.map { Pair(it.packageName, it) })
        return hashMap.values.sortedBy { it.appName }
    }
}
