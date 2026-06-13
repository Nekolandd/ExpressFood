package com.example.expressfood.ui.menu

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.expressfood.data.local.entity.ProductEntity
import com.example.expressfood.data.repository.CartRepository
import com.example.expressfood.data.repository.ProductRepository
import io.mockk.coEvery
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
class MenuViewModelTest {

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
    fun setNameFilter_showsMatchingProducts() = runTest {
        val productRepository = mockk<ProductRepository>()
        val cartRepository = mockk<CartRepository>(relaxed = true)
        val products = listOf(
            ProductEntity("1", "Pizza Margarita", 12.99, "Tomate", 25, 4.7, "url", true),
            ProductEntity("2", "Hamburguesa Clásica", 9.50, "Carne", 20, 4.5, "url", true)
        )

        every { productRepository.getEnabledProducts() } returns flowOf(products)
        every { cartRepository.getCartItemCount() } returns flowOf(0)
        coEvery { productRepository.ensureLocalMenu() } returns Unit

        val viewModel = MenuViewModel(productRepository, cartRepository)
        backgroundScope.launch { viewModel.products.collect {} }
        advanceUntilIdle()

        viewModel.setNameFilter("pizza")
        advanceUntilIdle()

        assertEquals(1, viewModel.products.value.size)
        assertEquals("Pizza Margarita", viewModel.products.value.first().name)
    }
}
