package com.example.skeleton.ui

import android.content.Context
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import io.reactivex.disposables.CompositeDisposable
import com.example.skeleton.ui.base.BaseController
import com.example.skeleton.MyService
import com.example.skeleton.helper.LP
import com.example.skeleton.model.UsageSummary
import com.example.skeleton.widget.UsageRecyclerAdapter

class MainScreen : BaseController() {
    private val mSubscriptions = CompositeDisposable()
    private var mMessage: TextView? = null

    //region Lifecycle
    //---------------------------------------------------------------
    override fun onCreateView(context: Context): View {
        context.startService(Intent(context, MyService::class.java))

        return setup(context)
    }

    private fun setup(context: Context): View {
        val testData = arrayOf(
                UsageSummary("chrome", "com.android.chrome", 82041L, 4420L),
                UsageSummary("System UI", "com.android.systemui", 204124L, 1421L),
                UsageSummary("Phone", "com.android.phone", 421124L, 14251L)
        )
        val layout = LinearLayout(context)
        val recycler = RecyclerView(context)

        layout.orientation = LinearLayout.VERTICAL

        recycler.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = UsageRecyclerAdapter(testData)
        }

        layout.addView(recycler, LP.linear(LP.MATCH_PARENT, LP.WRAP_CONTENT).build())

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
}
