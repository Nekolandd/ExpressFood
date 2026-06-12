package com.example.expressfood.data.repository

import com.example.expressfood.data.local.AppDatabase
import com.example.expressfood.data.local.dao.CartDao
import com.example.expressfood.data.local.entity.CartItemEntity
import com.example.expressfood.domain.model.Product
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CartRepositoryTest {

    private val database: AppDatabase = mockk()
    private val cartDao: CartDao = mockk(relaxed = true)
    private lateinit var repository: CartRepository

    private val product = Product("p1", "Pizza", 10.0, "Queso", 20, 4.5, "url", true)

    @Before
    fun setup() {
        every { database.cartDao() } returns cartDao
        repository = CartRepository(database)
    }

    @Test
    fun addProduct_insertsWhenNotInCart() = runTest {
        coEvery { cartDao.getByProductId("p1") } returns null

        repository.addProduct(product)

        coVerify { cartDao.insert(CartItemEntity(productId = "p1", quantity = 1)) }
    }
}
