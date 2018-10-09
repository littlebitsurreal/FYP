package com.example.skeleton.helper

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log

object PackageHelper {
    fun getAppName(context: Context, packageName: String): String? {
        val packageManager = context.packageManager
        try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            return packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            return null
        }
    }

    fun getAppIcon(context: Context, packageName: String): Drawable? {
        try {
            val icon = context.packageManager.getApplicationIcon(packageName)
            return icon
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("getAppIcon", "${e.message} - ${e.localizedMessage}")
            return null
        }
    }
}
