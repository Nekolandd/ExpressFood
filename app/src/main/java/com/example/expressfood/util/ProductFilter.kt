package com.example.expressfood.util

import com.example.expressfood.domain.model.Product

// filtra el menú mientras el usuario escribe en la barra de búsqueda.
object ProductFilter {

    fun filterByName(products: List<Product>, query: String): List<Product> {
        if (query.isBlank()) return products
        val normalized = query.trim().lowercase()
        return products.filter { it.name.lowercase().contains(normalized) }
    }

    fun filterByIngredient(products: List<Product>, query: String): List<Product> {
        if (query.isBlank()) return products
        val normalized = query.trim().lowercase()
        return products.filter { it.ingredients.lowercase().contains(normalized) }
    }

    fun applyFilters(
        products: List<Product>,
        nameQuery: String,
        ingredientQuery: String
    ): List<Product> {
        return filterByIngredient(
            filterByName(products, nameQuery),
            ingredientQuery
        )
    }
}
