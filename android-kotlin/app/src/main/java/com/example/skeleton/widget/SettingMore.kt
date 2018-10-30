package com.example.skeleton.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.skeleton.R
import com.example.skeleton.helper.LP

@SuppressLint("ResourceType")
class SettingMore(context: Context) : RelativeLayout(context) {
    val title = TextView(context)
    val content = TextView(context)
    val more = ImageView(context)

    init {
        title.apply {
            textSize = 16f
            setTextColor(Color.parseColor("#404040"))
            id = 1
        }
        content.apply {
            id = 2
        }
        more.apply {
            id = 3
            setImageDrawable(resources.getDrawable(R.drawable.ic_chevron_right_black_24dp, null))
        }
        addView(title)
        addView(content, LP.relative(LP.MATCH_PARENT, LP.WRAP_CONTENT)
                .toLeftOf(more.id)
                .belowOf(title.id)
                .build())
        addView(more, LP.relative(LP.WRAP_CONTENT, LP.WRAP_CONTENT)
                .alignParentEnd()
                .alignCenterVertical()
                .build())
    }

    fun setPadding(margin: Int) {
        setPadding(margin * 2, margin, margin * 2, margin)
        more.setPadding(margin * 2, 0, 0, 0)
    }
}
