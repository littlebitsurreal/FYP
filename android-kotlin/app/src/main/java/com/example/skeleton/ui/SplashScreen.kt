package com.example.skeleton.ui

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.example.skeleton.R
import com.example.skeleton.helper.LP
import com.example.skeleton.helper.ResourceHelper.font
import com.example.skeleton.ui.base.BaseController

class SplashScreen : BaseController() {
    override fun onCreateView(context: Context): View {
        val layout = FrameLayout(context)
        val tv = TextView(context)
        tv.typeface = font(R.font.barlow_condensed_thin)
        tv.textSize = 32f
        tv.setText(R.string.splash_title)
        layout.addView(tv, LP.frame(LP.WRAP_CONTENT, LP.WRAP_CONTENT, Gravity.CENTER).build())
        return layout
    }
}
