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
            binding.tvOrderId.text = "#${order.id.take(8).uppercase()}"
            binding.tvClient.text = order.userName.ifBlank { order.userId }
            binding.tvDate.text = dateFormat.format(Date(order.createdAt))
            binding.tvTotal.text = String.format(Locale.getDefault(), "$%.2f", order.total)
            StatusBadgeHelper.applyStatus(binding.chipStatus, order.status)

            val isEditable = order.status == OrderStatus.PENDING || order.status == OrderStatus.ON_THE_WAY
            binding.btnChangeStatus.visibility = if (isEditable) android.view.View.VISIBLE else android.view.View.GONE

            if (isEditable) {
                binding.btnChangeStatus.setOnClickListener { view ->
                    val context = view.context
                    val popup = androidx.appcompat.widget.PopupMenu(context, view)
                    
                    if (order.status == OrderStatus.PENDING) {
                        popup.menu.add(0, 1, 0, "En camino")
                    } else if (order.status == OrderStatus.ON_THE_WAY) {
                        popup.menu.add(0, 2, 0, "Entregada")
                    }
                    popup.menu.add(0, 3, 1, "Cancelar")

                    popup.setOnMenuItemClickListener { menuItem ->
                        val nextStatus = when (menuItem.itemId) {
                            1 -> OrderStatus.ON_THE_WAY
                            2 -> OrderStatus.DELIVERED
                            3 -> OrderStatus.CANCELLED
                            else -> null
                        }
                        if (nextStatus != null) {
                            onStatusChange(order, nextStatus)
                        }
                        true
                    }
                    popup.show()
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Order, newItem: Order) = oldItem == newItem
    }
}
