package com.example.skeleton.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import io.reactivex.disposables.CompositeDisposable
import com.example.skeleton.ui.base.BaseController
import com.example.skeleton.MyService
import com.example.skeleton.R
import com.example.skeleton.helper.LP
import com.example.skeleton.helper.ResourceHelper.color
import com.example.skeleton.helper.ResourceHelper.dp
import com.example.skeleton.widget.ActionBar

class MainScreen : BaseController() {
    private val mSubscriptions = CompositeDisposable()
    private var mUnlockOverview: TextView? = null
    private var mUsageOverview: TextView? = null
    private var mMessage: TextView? = null

    //region Lifecycle
    //---------------------------------------------------------------
    override fun onCreateView(context: Context): View {
        context.startService(Intent(context, MyService::class.java))

        return setup(context)
    }

    private fun setup(context: Context): View {
        val layout = LinearLayout(context)
        val actionBar = ActionBar(context)
        val overviewLayout = LinearLayout(context)
        val unlockOverview = TextView(context)
        val usageOverview = TextView(context)

        mUnlockOverview = unlockOverview
        mUsageOverview = usageOverview

        layout.apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(color(R.color.accent))
        }

        actionBar.apply {
            setTitle(R.string.app_name)
            addRightButton(R.drawable.ic_settings_24dp, onSettingClick)
        }

        unlockOverview.apply {
            gravity = Gravity.CENTER_HORIZONTAL
            text = resources?.getString(R.string.main_unlockoverview, "12")
        }
        usageOverview.apply {
            gravity = Gravity.CENTER_HORIZONTAL
            text = resources?.getString(R.string.main_usageoverview, "30 min")
        }
        overviewLayout.apply {
            val line = View(context).apply { setBackgroundColor(Color.parseColor("#CCCCCC")) }
            addView(unlockOverview, LP.linear(0, LP.WRAP_CONTENT).setWeight(1).build())
            addView(line, LP.linear(dp(1), LP.MATCH_PARENT).setMargins(0, dp(8), 0, dp(8)).build())
            addView(usageOverview, LP.linear(0, LP.WRAP_CONTENT).setWeight(1).build())
        }

        layout.apply {
            addView(actionBar, LP.linear(LP.MATCH_PARENT, LP.WRAP_CONTENT).build())
            addView(overviewLayout, LP.linear(LP.MATCH_PARENT, LP.WRAP_CONTENT)
                    .setMargins(0, dp(30), 0, dp(30))
                    .build())
        }

        return layout
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
    }

    override fun onDetach(view: View) {
        super.onDetach(view)
        mSubscriptions.clear()
    }
    //---------------------------------------------------------------
    //endregion

    //region UI Events
    fun setUnlock(str: String) {
        mUnlockOverview?.text = resources?.getString(R.string.main_unlockoverview, str)
    }

    fun setUsage(str: String) {
        mUsageOverview?.text = resources?.getString(R.string.main_usageoverview, str)
    }

    val onSettingClick = View.OnClickListener { }
    //endregion
}
