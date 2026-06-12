package com.example.expressfood.ui.orders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.expressfood.databinding.ItemDailyReportBinding
import com.example.expressfood.util.ReportHelper
import java.util.Locale

class DailyReportAdapter : ListAdapter<ReportHelper.DailySummary, DailyReportAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDailyReportBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemDailyReportBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(summary: ReportHelper.DailySummary) {
            binding.tvDate.text = summary.displayDate
            binding.tvOrderCount.text = "${summary.orderCount} órdenes"
            binding.tvTotal.text = String.format(Locale.getDefault(), "$%.2f", summary.totalAmount)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ReportHelper.DailySummary>() {
        override fun areItemsTheSame(
            oldItem: ReportHelper.DailySummary,
            newItem: ReportHelper.DailySummary
        ) = oldItem.dateKey == newItem.dateKey

        override fun areContentsTheSame(
            oldItem: ReportHelper.DailySummary,
            newItem: ReportHelper.DailySummary
        ) = oldItem == newItem
    }
}
