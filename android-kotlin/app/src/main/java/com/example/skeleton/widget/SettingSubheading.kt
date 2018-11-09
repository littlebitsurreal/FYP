package com.example.skeleton.widget

import android.content.Context
import android.widget.TextView
import com.example.skeleton.R
import com.example.skeleton.helper.ResourceHelper

class SettingSubheading(context: Context) : TextView(context) {
    init {
        textSize = 12f
        setTextColor(ResourceHelper.color(R.color.primary))
    }
    fun setPadding(margin: Int) {
        setPadding(2 * margin, margin, 2 * margin, 0)
    }
}
