package com.example.expressfood.util

import com.example.expressfood.domain.model.Product
import org.junit.Assert.assertEquals
import org.junit.Test

class ProductFilterTest {

    private val items = listOf(
        Product("1", "Pizza Margarita", 12.99, "Tomate, mozzarella", 25, 4.7, "url", true),
        Product("2", "Hamburguesa Clásica", 9.50, "Carne, lechuga, tomate", 20, 4.5, "url", true),
        Product("3", "Ensalada César", 8.25, "Lechuga romana, crutones", 15, 4.3, "url", true)
    )

    @Test
    fun filterByName_returnsMatchingItems() {
        val result = ProductFilter.filterByName(items, "pizza")
        assertEquals(1, result.size)
        assertEquals("Pizza Margarita", result.first().name)
    }

    @Test
    fun filterByIngredient_returnsMatchingItems() {
        val result = ProductFilter.filterByIngredient(items, "mozzarella")
        assertEquals(1, result.size)
        assertEquals("Pizza Margarita", result.first().name)
    }
}
