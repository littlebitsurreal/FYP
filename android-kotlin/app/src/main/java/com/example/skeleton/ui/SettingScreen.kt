package com.example.skeleton.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.support.v7.widget.SwitchCompat
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.skeleton.MainApplication
import com.example.skeleton.R
import com.example.skeleton.helper.LP
import com.example.skeleton.helper.ResourceHelper
import com.example.skeleton.helper.ResourceHelper.dp
import com.example.skeleton.helper.Touchable
import com.example.skeleton.redux.ViewStore
import com.example.skeleton.ui.base.BaseController
import com.example.skeleton.widget.ActionBar
import com.example.skeleton.widget.SettingMore
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function

@SuppressLint("ResourceType")
class SettingScreen : BaseController() {
    private val margin = dp(13)
    private val mSubscriptions = CompositeDisposable()
    private var mStrictModeSwitch: SwitchCompat? = null
    private var mReminderSwitch: SwitchCompat? = null

    //redion Life Cycles
    override fun onCreateView(context: Context): View {
        return setup(context)
    }

    private fun setup(context: Context): View {
        val layout = LinearLayout(context)
        val actionBar = ActionBar(context)
        val reminderLayout = setupReminderLayout(context)
        val strictModeLayout = setupStrictModeLayout(context)
        val notTrackingList = SettingMore(context)

        layout.orientation = LinearLayout.VERTICAL

        actionBar.run {
            val title = TextView(context).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                typeface = ResourceHelper.font(R.font.barlow_condensed_medium)
                textSize = 22f
                setTextColor(ResourceHelper.color(R.color.primary))
                text = "Setting"
                Touchable.make(this@apply)
                setOnClickListener(onBackClick)
            }

            elevation = 2f
            setBackgroundColor(Color.WHITE)

            addLeftButton(R.drawable.ic_arrow_back_24dp, onBackClick)
            addLeftView(title)
        }

        notTrackingList.apply {
            title.text = "Not tracking list"
            content.text = "Do not take certain apps into usage count"
            setPadding(margin)
            setOnTouchListener(onNotTrackingListTouch)
        }

        layout.apply {
            addView(actionBar, LP.frame(LP.MATCH_PARENT, dp(50)).build())
            addView(reminderLayout)
            addView(strictModeLayout)
            addView(notTrackingList)
        }

        return layout
    }

    private fun setupReminderLayout(context: Context): RelativeLayout {
        return RelativeLayout(context).apply {
            val title = TextView(context)
            val content = TextView(context)
            val switch = object : SwitchCompat(context) {
                override fun setEnabled(enabled: Boolean) {
                    if (!enabled) {
                        title.alpha = 0.5f
                        content.alpha = 0.5f
                        alpha = 0.3f
                        isChecked = false
                    } else {
                        title.alpha = 1f
                        content.alpha = 1f
                        alpha = 1f
                    }
                    super.setEnabled(enabled)
                }
            }
            title.apply {
                textSize = 16f
                setTextColor(Color.parseColor("#404040"))
                id = 1
            }
            content.apply {
                id = 2
            }
            switch.apply {
                isSaveEnabled = false
                id = 12497
                trackTintList = ColorStateList(
                        arrayOf(intArrayOf(android.R.attr.state_checked),
                                intArrayOf(-android.R.attr.state_checked)),
                        intArrayOf(Color.parseColor("#47d147"),
                                Color.parseColor("#A3B0BA")))
            }
            setPadding(margin * 2, margin, margin * 2, margin)
            switch.setPadding(margin * 2, 0, 0, 0)
            addView(title)
            addView(content, LP.relative(LP.MATCH_PARENT, LP.WRAP_CONTENT)
                    .toLeftOf(switch.id)
                    .belowOf(title.id)
                    .build())
            addView(switch, LP.relative(LP.WRAP_CONTENT, LP.WRAP_CONTENT)
                    .alignParentEnd()
                    .alignCenterVertical()
                    .build())

            mReminderSwitch = switch
            title.text = "Over-use reminder"
            content.text = "Remind you if your usage exceeds limit"
            switch.setOnCheckedChangeListener(onReminderSwitchChange)
            setOnTouchListener(onReminderTouch)
        }
    }

    private fun setupStrictModeLayout(context: Context): RelativeLayout {
        return RelativeLayout(context).apply {
            val title = TextView(context)
            val content = TextView(context)
            val switch = object : SwitchCompat(context) {
                override fun setEnabled(enabled: Boolean) {
                    if (!enabled) {
                        title.alpha = 0.5f
                        content.alpha = 0.5f
                        alpha = 0.3f
                        isChecked = false
                    } else {
                        title.alpha = 1f
                        content.alpha = 1f
                        alpha = 1f
                    }
                    super.setEnabled(enabled)
                }
            }
            title.apply {
                textSize = 16f
                setTextColor(Color.parseColor("#404040"))
                id = 1
            }
            content.apply {
                id = 2
            }
            switch.apply {
                isSaveEnabled = false
                id = 828282
                trackTintList = ColorStateList(
                        arrayOf(intArrayOf(android.R.attr.state_checked),
                                intArrayOf(-android.R.attr.state_checked)),
                        intArrayOf(Color.parseColor("#47d147"),
                                Color.parseColor("#A3B0BA")))
            }
            setPadding(margin * 2, margin, margin * 2, margin)
            switch.setPadding(margin * 2, 0, 0, 0)
            addView(title)
            addView(content, LP.relative(LP.MATCH_PARENT, LP.WRAP_CONTENT)
                    .toLeftOf(switch.id)
                    .belowOf(title.id)
                    .build())
            addView(switch, LP.relative(LP.WRAP_CONTENT, LP.WRAP_CONTENT)
                    .alignParentEnd()
                    .alignCenterVertical()
                    .build())

            mStrictModeSwitch = switch
            title.setText(R.string.setting_strictmode_title)
            content.setText(R.string.setting_strictmode_content)
            switch.setOnCheckedChangeListener(onStrictModeSwitchChange)
            setOnTouchListener(onStrictModeTouch)
            switch.isEnabled = false
        }
    }

    override fun onAttach(view: View) {
        super.onAttach(view)

        mSubscriptions.add(MainApplication.store().observe(MainApplication.store().view)
                .observeOn(AndroidSchedulers.mainThread())
                .map(mapReminder)
                .distinctUntilChanged()
                .subscribe(consumeReminder))
        mSubscriptions.add(MainApplication.store().observe(MainApplication.store().view)
                .observeOn(AndroidSchedulers.mainThread())
                .map(mapStrictMode)
                .distinctUntilChanged()
                .subscribe(consumeStrictMode))
    }

    override fun onDetach(view: View) {
        mSubscriptions.clear()
        super.onDetach(view)
    }
    //endregion

    //region UI Events
    private val onReminderSwitchChange = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        MainApplication.store().view.dispatch(ViewStore.Action.setReminder(isChecked))

        mStrictModeSwitch?.isEnabled = isChecked
    }
    private val onReminderTouch = View.OnTouchListener { v, event ->
        if (event?.action == MotionEvent.ACTION_UP) {
            mReminderSwitch?.isChecked = !(mReminderSwitch?.isChecked
                    ?: return@OnTouchListener true)
            return@OnTouchListener false
        }
        true
    }

    private val onStrictModeSwitchChange = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        MainApplication.store().view.dispatch(ViewStore.Action.setStrictMode(isChecked))
    }
    private val onStrictModeTouch = View.OnTouchListener { v, event ->
        if (event?.action == MotionEvent.ACTION_UP) {
            mStrictModeSwitch?.isChecked = !(mStrictModeSwitch?.isChecked
                    ?: return@OnTouchListener true)
            return@OnTouchListener false
        }
        true
    }
    private val onNotTrackingListTouch = View.OnTouchListener { v, event ->
        if (event?.action == MotionEvent.ACTION_UP) {
            pushController(NotTrackingListScreen())
            return@OnTouchListener false
        }
        true
    }
    private val onBackClick = View.OnClickListener {
        popController()
    }
    //endregion

    //region redux
    private val mapReminder = Function<ViewStore.State, Boolean> { state ->
        state.reminderOn
    }
    private val consumeReminder = Consumer<Boolean> { isChecked ->
        mReminderSwitch?.isChecked = isChecked
    }
    private val mapStrictMode = Function<ViewStore.State, Boolean> { state ->
        state.strictModeOn
    }
    private val consumeStrictMode = Consumer<Boolean> { isChecked ->
        mStrictModeSwitch?.isChecked = isChecked
    }
    //endregion
}
