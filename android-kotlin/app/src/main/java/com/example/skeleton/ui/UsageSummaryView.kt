package com.example.skeleton.ui

import android.content.Context
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.skeleton.R
import com.example.skeleton.helper.LP
import com.example.skeleton.helper.PackageHelper.getAppIcon
import com.example.skeleton.helper.ResourceHelper.dp
import com.example.skeleton.model.UsageSummary

class UsageSummaryView(context: Context) : LinearLayout(context) {
    val icon = ImageView(context)
    val appName = TextView(context)
    val usageSummary = TextView(context)

    init {
        setup()
    }

    private fun setup() {
        val contentContainer = LinearLayout(context)

        setPadding(dp(10), dp(10), dp(10), dp(10))

        contentContainer.apply {
            orientation = VERTICAL
            addView(appName)
            addView(usageSummary)
        }

        addView(icon, LP.linear(dp(50), dp(50), Gravity.CENTER_VERTICAL).build())
        addView(contentContainer)
    }

    fun bind(u: UsageSummary) {
        icon.setImageDrawable(getAppIcon(context, u.packageName))
        appName.text = u.appName
        usageSummary.text = resources.getString(R.string.messageview_time_hint, u.useTimeAverage / 1000)
    }
}
