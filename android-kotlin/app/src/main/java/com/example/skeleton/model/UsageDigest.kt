package com.example.skeleton.model

import android.content.Context
import com.example.skeleton.helper.CalendarHelper
import com.example.skeleton.helper.CsvHelper
import com.example.skeleton.helper.Logger
import com.example.skeleton.helper.ScreenUnlockHelper
import com.example.skeleton.iface.SerializableToJson
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

@Suppress("LiftReturnOrAssignment")
data class UsageDigest(
        val day: String,
        val summary: List<UsageSummary>,
        val totalTime: Long,
        val unlockCount: Int
) : SerializableToJson {
    override fun toJson(): JSONObject {
        val summaryList = JSONArray()
        for (i in summary) {
            summaryList.put(i.toJson())
        }
        return JSONObject()
                .put("day", day)
                .put("totalTime", totalTime)
                .put("unlockCount", unlockCount)
                .put("summaryList", summaryList)
    }

    companion object {
        const val TAG = "UsageDigest"

        fun fromJson(json: JSONObject): UsageDigest {
            val summary = arrayListOf<UsageSummary>()
            val summaryJson = json.getJSONArray("summaryList")
            for (i in 0..(summaryJson.length() - 1)) {
                val item = summaryJson.getJSONObject(i)
                try {
                    summary.add(UsageSummary.fromJson(item))
                } catch (e: Exception) {
                    Logger.d(TAG, "fromJson failed: i = $i")
                }
            }
            return UsageDigest(json.getString("day"), summary, json.getLong("totalTime"), json.getInt("unlockCount"))
        }

        fun make(context: Context, day: String): UsageDigest? {
            val file = File(context.filesDir.path + "/" + day)
            val records = CsvHelper.read(file)
            Logger.d(TAG, "make $day - ${records.size}")
            if (records.isEmpty()) {
                return null
            }
            val summary = UsageSummary.getSummary(context, records)
            return UsageDigest(day, summary, summary.map { it.useTimeTotal }.sum(), ScreenUnlockHelper.getUnlockCount(context, day))
        }

        fun load(context: Context, day: String): UsageDigest? {
            Logger.d(TAG, "load   $day   ${CalendarHelper.getDayCondensed(System.currentTimeMillis())}")
            if (day == CalendarHelper.getDayCondensed(System.currentTimeMillis())) {
                return make(context, day)
            }

            try {
                val pref = context.getSharedPreferences(TAG, Context.MODE_PRIVATE)
                val str = pref.getString(day, null) ?: throw Exception("day $day not found")
                return UsageDigest.fromJson(JSONObject(str))
            } catch (e: Exception) {
                Logger.d(TAG, "load digest($day) failed - ${e.message}")
                val digest = make(context, day)
                if (digest != null) {
                    save(context, digest)
                }
                return digest
            }
        }

        private fun save(context: Context, digest: UsageDigest) {
            val pref = context.getSharedPreferences(TAG, Context.MODE_PRIVATE).edit()
            pref.putString(digest.day, digest.toJson().toString())
            pref.apply()
        }
    }
}
