package com.example.expressfood.ui.cart

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.expressfood.data.repository.CartRepository
import com.example.expressfood.data.repository.OrderRepository
import com.example.expressfood.domain.model.CartItem
import com.example.expressfood.domain.model.Order
import com.example.expressfood.domain.model.OrderStatus
import com.example.expressfood.domain.model.Product
import com.example.expressfood.util.ConnectivityObserver
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var cartRepository: CartRepository
    private lateinit var orderRepository: OrderRepository
    private lateinit var connectivityObserver: ConnectivityObserver

    private val sampleItems = listOf(
        CartItem(Product("1", "Pizza", 10.0, "Queso", 20, 4.5, "url", true), 2)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        cartRepository = mockk(relaxed = true)
        orderRepository = mockk(relaxed = true)
        connectivityObserver = mockk(relaxed = true)

        every { cartRepository.getCartItems() } returns flowOf(sampleItems)
        every { connectivityObserver.isCurrentlyOnline() } returns true
        coEvery {
            orderRepository.createOrderFromCart(any(), any(), any(), any())
        } returns Order("o1", "u1", "User", 0L, OrderStatus.PENDING, 20.0, 2.6, 22.6, true, emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun processOrder_clearsCartOnSuccess() = runTest {
        val viewModel = CartViewModel(cartRepository, orderRepository, connectivityObserver, "u1", "User")
        backgroundScope.launch { viewModel.cartItems.collect {} }
        advanceUntilIdle()
        viewModel.processOrder()
        advanceUntilIdle()
        coVerify { cartRepository.clearCart() }
    }
}
