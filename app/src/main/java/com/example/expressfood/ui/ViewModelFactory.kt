package com.example.expressfood.ui

import android.content.Context
import com.example.expressfood.ExpressFoodApplication
import com.example.expressfood.domain.model.OrderStatus
import com.example.expressfood.ui.admin.AdminOrdersViewModel
import com.example.expressfood.ui.admin.AdminProductsViewModel
import com.example.expressfood.ui.admin.AdminReportViewModel
import com.example.expressfood.ui.cart.CartViewModel
import com.example.expressfood.ui.menu.MenuViewModel
import com.example.expressfood.ui.orders.ClientOrdersViewModel
import com.example.expressfood.ui.orders.ClientReportViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ExpressFoodViewModelFactory(
    private val app: ExpressFoodApplication,
    private val userId: String = FirebaseAuth.getInstance().currentUser?.uid.orEmpty(),
    private val userName: String = FirebaseAuth.getInstance().currentUser?.displayName.orEmpty()
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MenuViewModel::class.java) ->
                MenuViewModel(app.productRepository, app.cartRepository) as T
            modelClass.isAssignableFrom(CartViewModel::class.java) ->
                CartViewModel(
                    app.cartRepository,
                    app.orderRepository,
                    app.connectivityObserver,
                    userId,
                    userName
                ) as T
            modelClass.isAssignableFrom(ClientOrdersViewModel::class.java) ->
                ClientOrdersViewModel(app.orderRepository, userId) as T
            modelClass.isAssignableFrom(ClientReportViewModel::class.java) ->
                ClientReportViewModel(app.orderRepository, userId) as T
            modelClass.isAssignableFrom(AdminOrdersViewModel::class.java) ->
                AdminOrdersViewModel(app.orderRepository) as T
            modelClass.isAssignableFrom(AdminReportViewModel::class.java) ->
                AdminReportViewModel(app.orderRepository) as T
            modelClass.isAssignableFrom(AdminProductsViewModel::class.java) ->
                AdminProductsViewModel(app.productRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }

    companion object {
        fun from(context: Context): ExpressFoodViewModelFactory {
            val app = context.applicationContext as ExpressFoodApplication
            return ExpressFoodViewModelFactory(app)
        }
    }
}

fun OrderStatus.displayName(): String = when (this) {
    OrderStatus.CREATED -> "Creada"
    OrderStatus.PENDING -> "Pendiente"
    OrderStatus.ON_THE_WAY -> "En camino"
    OrderStatus.DELIVERED -> "Entregada"
    OrderStatus.CANCELLED -> "Cancelada"
}
