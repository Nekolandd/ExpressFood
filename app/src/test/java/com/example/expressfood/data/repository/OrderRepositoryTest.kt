package com.example.expressfood.data.repository

import com.example.expressfood.data.local.AppDatabase
import com.example.expressfood.data.local.dao.OrderDao
import com.example.expressfood.data.remote.FirestoreOrderDataSource
import com.example.expressfood.domain.model.CartItem
import com.example.expressfood.domain.model.Product
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrderRepositoryTest {

    private val database: AppDatabase = mockk()
    private val orderDao: OrderDao = mockk(relaxed = true)
    private val remote: FirestoreOrderDataSource = mockk(relaxed = true)
    private lateinit var repository: OrderRepository

    private val cartItems = listOf(
        CartItem(Product("p1", "Pizza", 10.0, "Queso", 20, 4.5, "url", true), 1)
    )

    @Before
    fun setup() {
        every { database.orderDao() } returns orderDao
        repository = OrderRepository(database, remote)
    }

    @Test
    fun createOrderFromCart_offlineMarksOrderAsNotSynced() = runTest {
        val order = repository.createOrderFromCart("u1", "User", cartItems, isOnline = false)

        assertFalse(order.synced)
        coVerify { orderDao.insertOrder(any()) }
        coVerify(exactly = 0) { remote.uploadOrder(any()) }
    }
}
