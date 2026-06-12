package com.example.expressfood.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.expressfood.databinding.ItemAdminOrderBinding
import com.example.expressfood.domain.model.Order
import com.example.expressfood.domain.model.OrderStatus
import com.example.expressfood.ui.common.StatusBadgeHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminOrderAdapter(
    private val onStatusChange: (Order, OrderStatus) -> Unit
) : ListAdapter<Order, AdminOrderAdapter.AdminOrderViewHolder>(DiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminOrderViewHolder {
        val binding = ItemAdminOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AdminOrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdminOrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AdminOrderViewHolder(
        private val binding: ItemAdminOrderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            binding.tvClient.text = order.userName.ifBlank { order.userId }
            binding.tvDate.text = dateFormat.format(Date(order.createdAt))
            binding.tvTotal.text = String.format(Locale.getDefault(), "$%.2f", order.total)
            StatusBadgeHelper.applyStatus(binding.chipStatus, order.status)

            binding.btnOnTheWay.setOnClickListener {
                onStatusChange(order, OrderStatus.ON_THE_WAY)
            }
            binding.btnDelivered.setOnClickListener {
                onStatusChange(order, OrderStatus.DELIVERED)
            }
            binding.btnCancel.setOnClickListener {
                onStatusChange(order, OrderStatus.CANCELLED)
            }

            val canProgress = order.status == OrderStatus.PENDING
            binding.btnOnTheWay.isEnabled = canProgress
            binding.btnDelivered.isEnabled = order.status == OrderStatus.ON_THE_WAY
            binding.btnCancel.isEnabled =
                order.status == OrderStatus.PENDING || order.status == OrderStatus.ON_THE_WAY
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Order, newItem: Order) = oldItem == newItem
    }
}
