package com.example.expressfood.ui.orders

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.expressfood.data.repository.OrderRepository
import com.example.expressfood.domain.model.Order
import com.example.expressfood.domain.model.OrderStatus
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
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClientOrdersViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun setStatusFilter_returnsMatchingOrders() = runTest {
        val orderRepository = mockk<OrderRepository>()
        val orders = listOf(
            Order("o1", "u1", "User", 0L, OrderStatus.PENDING, 10.0, 1.3, 11.3, true, emptyList()),
            Order("o2", "u1", "User", 0L, OrderStatus.DELIVERED, 20.0, 2.6, 22.6, true, emptyList())
        )
        every { orderRepository.getOrdersForUser("u1") } returns flowOf(orders)

        val viewModel = ClientOrdersViewModel(orderRepository, "u1")
        backgroundScope.launch { viewModel.orders.collect {} }
        advanceUntilIdle()

        viewModel.setStatusFilter(OrderStatus.PENDING)
        advanceUntilIdle()

        assertEquals(1, viewModel.orders.value.size)
        assertEquals(OrderStatus.PENDING, viewModel.orders.value.first().status)
    }
}
