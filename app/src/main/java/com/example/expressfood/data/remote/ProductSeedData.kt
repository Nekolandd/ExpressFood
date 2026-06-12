package com.example.expressfood.data.remote

import com.example.expressfood.domain.model.Product

object ProductSeedData {

    val defaultProducts: List<Product> = listOf(
        Product(
            id = "seed_pizza_margarita",
            name = "Pizza Margarita",
            price = 12.99,
            ingredients = "Tomate, mozzarella, albahaca, aceite de oliva",
            estimatedTime = 25,
            rating = 4.7,
            imageUrl = "https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=400",
            enabled = true
        ),
        Product(
            id = "seed_hamburguesa_clasica",
            name = "Hamburguesa Clásica",
            price = 9.50,
            ingredients = "Carne de res, lechuga, tomate, queso, pan artesanal",
            estimatedTime = 20,
            rating = 4.5,
            imageUrl = "https://images.unsplash.com/photo-1568901346715-4c5d5a4a8b8e?w=400",
            enabled = true
        ),
        Product(
            id = "seed_sushi_variado",
            name = "Sushi Variado",
            price = 18.00,
            ingredients = "Salmón, atún, arroz, alga nori, aguacate",
            estimatedTime = 30,
            rating = 4.8,
            imageUrl = "https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?w=400",
            enabled = true
        ),
        Product(
            id = "seed_ensalada_cesar",
            name = "Ensalada César",
            price = 8.25,
            ingredients = "Lechuga romana, crutones, parmesano, aderezo césar",
            estimatedTime = 15,
            rating = 4.3,
            imageUrl = "https://images.unsplash.com/photo-1546793665-c74683f339c1?w=400",
            enabled = true
        ),
        Product(
            id = "seed_tacos_mexicanos",
            name = "Tacos Mexicanos",
            price = 10.75,
            ingredients = "Tortilla de maíz, carne, cilantro, cebolla, salsa",
            estimatedTime = 18,
            rating = 4.6,
            imageUrl = "https://images.unsplash.com/photo-1565299585323-38a6c5dc0793?w=400",
            enabled = true
        )
    )
}
