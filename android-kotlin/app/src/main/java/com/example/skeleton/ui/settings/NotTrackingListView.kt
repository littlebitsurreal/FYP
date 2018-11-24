package com.example.skeleton.ui.settings

import android.content.Context
import android.support.v7.widget.AppCompatCheckBox
import android.view.Gravity
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.skeleton.helper.LP
import com.example.skeleton.helper.PackageHelper
import com.example.skeleton.helper.ResourceHelper
import com.example.skeleton.helper.ResourceHelper.dp
import com.example.skeleton.model.NotTrackingRecord

class NotTrackingListView(context: Context, onCheckChange: ((NotTrackingRecord, Boolean) -> Unit)?) : LinearLayout(context) {
    val margin = ResourceHelper.dp(14)
    val icon = ImageView(context)
    val appName = TextView(context)
    val checkBox = AppCompatCheckBox(context)
    var packageName: String = ""
    var record: NotTrackingRecord? = null

    init {
        setup(onCheckChange)
    }

    private fun setup(listener: ((NotTrackingRecord, Boolean) -> Unit)?) {
        setPadding(margin, margin, margin, margin)

        icon.apply {
            id = id@ 1
        }

        appName.apply {
            textSize = 16f
            id = id@ 3
        }

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            listener?.invoke(record ?: return@setOnCheckedChangeListener, isChecked)
            id = id@ 5
        }

        addView(icon, LP.linear(ResourceHelper.dp(40), ResourceHelper.dp(40))
                .setGravity(Gravity.CENTER_VERTICAL)
                .setMargins(margin, 0, margin, 0)
                .build())
        addView(appName, LP.linear(0, LP.WRAP_CONTENT)
                .setGravity(Gravity.CENTER_VERTICAL)
                .setWeight(1)
                .build())
        addView(checkBox, LP.linear(dp(24), dp(24))
                .setGravity(Gravity.CENTER_VERTICAL)
                .setMargins(0, 0, margin, 0)
                .build())

        val lp = LinearLayout.LayoutParams(LP.MATCH_PARENT, LP.WRAP_CONTENT)
        layoutParams = lp
        isClickable = true

        setOnTouchListener { _, event ->
            if (event?.action == MotionEvent.ACTION_UP) {
                checkBox.isChecked = !checkBox.isChecked
                return@setOnTouchListener false
            }
            true
        }
    }

    fun bind(record: NotTrackingRecord) {
        icon.setImageDrawable(PackageHelper.getAppIcon(context, record.packageName))
        appName.text = record.appName
        packageName = record.packageName
        checkBox.isChecked = record.isIgnored
        this.record = record
    }
}
