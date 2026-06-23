package com.example.expressfood.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expressfood.data.local.toDomain
import com.example.expressfood.data.repository.CartRepository
import com.example.expressfood.data.repository.ProductRepository
import com.example.expressfood.domain.model.Product
import com.example.expressfood.util.ProductFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// muestra el menú, aplica filtros de búsqueda y agrega productos al carrito.
class MenuViewModel(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    init {
        // Carga el menú desde la base local (funciona también sin internet).
        viewModelScope.launch {
            productRepository.ensureLocalMenu()
        }
    }

    private val nameFilter = MutableStateFlow("")
    private val ingredientFilter = MutableStateFlow("")

    val cartItemCount: StateFlow<Int> = cartRepository.getCartItemCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val products: StateFlow<List<Product>> = combine(
        productRepository.getEnabledProducts(),
        nameFilter,
        ingredientFilter
    ) { entities, name, ingredient ->
        val products = entities.map { it.toDomain() }
        ProductFilter.applyFilters(products, name, ingredient)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nameQuery: StateFlow<String> = nameFilter.asStateFlow()
    val ingredientQuery: StateFlow<String> = ingredientFilter.asStateFlow()

    fun setNameFilter(query: String) {
        nameFilter.value = query
    }

    fun setIngredientFilter(query: String) {
        ingredientFilter.value = query
    }

    fun addToCart(product: Product, onAdded: () -> Unit) {
        viewModelScope.launch {
            cartRepository.addProduct(product)
            onAdded()
        }
    }
}
