package com.example.expressfood.ui.orders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.expressfood.databinding.ItemOrderBinding
import com.example.expressfood.domain.model.Order
import com.example.expressfood.ui.common.StatusBadgeHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderAdapter : ListAdapter<Order, OrderAdapter.OrderViewHolder>(DiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderViewHolder(
        private val binding: ItemOrderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            binding.tvOrderId.text = "ID: ${order.id.take(8)}..."
            binding.tvDate.text = dateFormat.format(Date(order.createdAt))
            binding.tvTotal.text = String.format(Locale.getDefault(), "$%.2f", order.total)
            StatusBadgeHelper.applyStatus(binding.chipStatus, order.status)
            val productsText = order.items.joinToString("\n") { item ->
                "${item.quantity}x ${item.productName}"
            }
            binding.tvProducts.text = productsText
            if (!order.synced) {
                binding.tvSyncStatus.text = "Pendiente de sincronización"
                binding.tvSyncStatus.visibility = android.view.View.VISIBLE
            } else {
                binding.tvSyncStatus.visibility = android.view.View.GONE
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Order, newItem: Order) = oldItem == newItem
    }
}
