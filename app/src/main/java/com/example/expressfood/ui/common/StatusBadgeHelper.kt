package com.example.expressfood.ui.common

import android.content.Context
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import com.example.expressfood.R
import com.example.expressfood.domain.model.OrderStatus
import com.google.android.material.chip.Chip

object StatusBadgeHelper {

    fun applyStatus(chip: Chip, status: OrderStatus) {
        chip.text = when (status) {
            OrderStatus.CREATED -> chip.context.getString(R.string.status_created)
            OrderStatus.PENDING -> chip.context.getString(R.string.status_pending)
            OrderStatus.ON_THE_WAY -> chip.context.getString(R.string.status_on_the_way)
            OrderStatus.DELIVERED -> chip.context.getString(R.string.status_delivered)
            OrderStatus.CANCELLED -> chip.context.getString(R.string.status_cancelled)
        }
        val colorRes = when (status) {
            OrderStatus.PENDING, OrderStatus.CREATED -> R.color.status_pending
            OrderStatus.ON_THE_WAY -> R.color.status_on_the_way
            OrderStatus.DELIVERED -> R.color.status_delivered
            OrderStatus.CANCELLED -> R.color.status_cancelled
        }
        chip.chipBackgroundColor = ContextCompat.getColorStateList(chip.context, colorRes)
        chip.setTextColor(ContextCompat.getColor(chip.context, R.color.white))
    }

    fun badgeDrawable(context: Context, status: OrderStatus): GradientDrawable {
        val colorRes = when (status) {
            OrderStatus.PENDING, OrderStatus.CREATED -> R.color.status_pending
            OrderStatus.ON_THE_WAY -> R.color.status_on_the_way
            OrderStatus.DELIVERED -> R.color.status_delivered
            OrderStatus.CANCELLED -> R.color.status_cancelled
        }
        return GradientDrawable().apply {
            cornerRadius = 24f
            setColor(ContextCompat.getColor(context, colorRes))
        }
    }
}
