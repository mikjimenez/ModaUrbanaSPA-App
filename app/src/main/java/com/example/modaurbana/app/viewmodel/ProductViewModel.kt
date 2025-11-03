package com.example.modaurbana.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.modaurbana.app.data.local.entity.ProductEntity
import com.example.modaurbana.app.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProductUiState(
    val isLoading: Boolean = false,
    val products: List<ProductEntity> = emptyList(),
    val selectedCategory: String = "Todos",
    val error: String? = null
)

class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProductRepository(application)

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState

    init {
        loadProducts()
    }

    /**
     * Carga todos los productos
     */
    fun loadProducts() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            repository.getAllProducts().collect { products ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    products = products,
                    error = null
                )
            }
        }
    }

    /**
     * Filtra productos por categoría
     */
    fun filterByCategory(category: String) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            isLoading = true
        )

        viewModelScope.launch {
            if (category == "Todos") {
                repository.getAllProducts().collect { products ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        products = products
                    )
                }
            } else {
                repository.getProductsByCategory(category).collect { products ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        products = products
                    )
                }
            }
        }
    }
}