package com.example.expressfood.data.repository

import com.example.expressfood.data.local.AppDatabase
import com.example.expressfood.data.local.toDomain
import com.example.expressfood.data.local.toEntity
import com.example.expressfood.data.remote.FirestoreProductDataSource
import com.example.expressfood.data.remote.ProductSeedData
import com.example.expressfood.domain.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// menú local, sincronización remota y CRUD de productos del admin.
class ProductRepository(
    private val database: AppDatabase,
    private val remote: FirestoreProductDataSource = FirestoreProductDataSource()
) {
    private val productDao get() = database.productDao()

    fun getEnabledProducts() = productDao.getEnabledProducts()

    fun getAllProductsFlow() = productDao.getAllProducts()

    // Si no hay menú guardado, lo descarga o usa datos por defecto.
    suspend fun ensureLocalMenu() = withContext(Dispatchers.IO) {
        if (productDao.count() > 0) return@withContext
        val products = try {
            remote.seedProductsIfEmpty()
        } catch (_: Exception) {
            ProductSeedData.defaultProducts
        }
        productDao.insertAll(products.map { it.toEntity() })
    }

    // actualiza el menú local con los productos más recientes de Firestore.
    suspend fun syncFromRemote(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val products = try {
                remote.seedProductsIfEmpty()
            } catch (_: Exception) {
                if (productDao.count() == 0) ProductSeedData.defaultProducts
                else return@runCatching
            }
            productDao.insertAll(products.map { it.toEntity() })
        }
    }

    suspend fun saveProduct(product: Product) = withContext(Dispatchers.IO) {
        remote.saveProduct(product)
        productDao.insert(product.toEntity())
    }

    suspend fun deleteProduct(productId: String) = withContext(Dispatchers.IO) {
        remote.deleteProduct(productId)
        productDao.deleteById(productId)
    }

    suspend fun getProductById(id: String) = withContext(Dispatchers.IO) {
        productDao.getById(id)?.toDomain()
    }
}
