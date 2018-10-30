package com.example.skeleton.model

import android.content.Context
import com.example.skeleton.helper.PackageHelper
import com.example.skeleton.iface.SerializableToJson
import org.json.JSONObject

data class UsageSummary(
        val appName: String,
        val packageName: String,
        val useTimeTotal: Long
) : SerializableToJson {
    override fun toJson(): JSONObject {
        return JSONObject()
                .put("appName", appName)
                .put("packageName", packageName)
                .put("useTimeTotal", useTimeTotal)
    }

    companion object {
        fun getSummary(context: Context, records: List<UsageRecord>): List<UsageSummary> {
            val dict = hashMapOf<String, Long>()
            for (r in records) {
                dict[r.packageName] = (dict[r.packageName] ?: 0) + r.duration
            }
            return dict.map {
                UsageSummary(PackageHelper.getAppName(context, it.key), it.key, it.value)
            }.sortedByDescending { it.useTimeTotal }
        }

        fun fromJson(json: JSONObject): UsageSummary {
            return UsageSummary(json.getString("appName"), json.getString("packageName"), json.getLong("useTimeTotal"))
        }
    }
}
