package com.example.expressfood.ui.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.expressfood.databinding.ItemCartBinding
import com.example.expressfood.domain.model.CartItem
import java.util.Locale

class CartAdapter(
    private val onIncrease: (String) -> Unit,
    private val onDecrease: (String) -> Unit,
    private val onRemove: (String) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartViewHolder(
        private val binding: ItemCartBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            binding.tvName.text = item.product.name
            binding.tvQuantity.text = item.quantity.toString()
            binding.tvUnitPrice.text = String.format(Locale.getDefault(), "Precio unitario: $%.2f", item.product.price)
            binding.tvLineTotal.text = String.format(Locale.getDefault(), "$%.2f", item.lineTotal)
            binding.ivProduct.load(item.product.imageUrl) {
                crossfade(true)
            }
            binding.btnIncrease.setOnClickListener { onIncrease(item.product.id) }
            binding.btnDecrease.setOnClickListener { onDecrease(item.product.id) }
            binding.btnRemove.setOnClickListener { onRemove(item.product.id) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem) =
            oldItem.product.id == newItem.product.id

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem) =
            oldItem == newItem
    }
}
