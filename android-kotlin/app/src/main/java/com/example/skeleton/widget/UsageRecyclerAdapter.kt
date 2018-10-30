package com.example.skeleton.widget

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.example.skeleton.helper.LP
import com.example.skeleton.model.UsageSummary
import com.example.skeleton.ui.UsageSummaryView

class UsageRecyclerAdapter(
        private var items: List<UsageSummary>,
        private var onClickListener: View.OnClickListener
) : RecyclerView.Adapter<UsageRecyclerAdapter.ViewHolder>() {
    var maxUsage = 0L

    class ViewHolder(val view: UsageSummaryView) : RecyclerView.ViewHolder(view) {
        fun bind(u: UsageSummary, maxUsage: Long) {
            view.bind(u, (u.useTimeTotal * 115 / maxUsage).toInt())
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val lp = RecyclerView.LayoutParams(LP.MATCH_PARENT, LP.WRAP_CONTENT)
        val view = UsageSummaryView(parent.context, onClickListener)
        view.layoutParams = lp
        maxUsage = items[0].useTimeTotal

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], maxUsage)
    }
}
