package com.example.skeleton.widget

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.example.skeleton.model.UsageSummary
import com.example.skeleton.ui.UsageSummaryView

class UsageRecyclerAdapter(private var items: Array<UsageSummary>) : RecyclerView.Adapter<UsageRecyclerAdapter.ViewHolder>() {
    class ViewHolder(val view: UsageSummaryView) : RecyclerView.ViewHolder(view) {
        fun bind(u: UsageSummary) {
            view.bind(u)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(UsageSummaryView(parent.context))
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }
}
