package com.example.modaurbana.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.modaurbana.app.data.local.entity.ProductEntity
import com.example.modaurbana.app.repository.ProductRepositoryHybrid
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProductUiState(
    val isLoading: Boolean = false,
    val products: List<ProductEntity> = emptyList(),
    val selectedCategory: String = "Todos",
    val error: String? = null,
    val successMessage: String? = null,
    val isLoadingFromApi: Boolean = false
)

class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProductRepositoryHybrid(application)

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState

    init {
        // Al iniciar, cargar productos con estrategia cache-first
        loadProductsWithCacheFirst()
    }

    /**
     * Carga productos usando estrategia cache-first
     * (Primero intenta local, luego API si es necesario)
     */
    private fun loadProductsWithCacheFirst() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val result = repository.getProductsWithCacheFirst()

            _uiState.value = result.fold(
                onSuccess = { products ->
                    _uiState.value.copy(
                        isLoading = false,
                        products = products,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al cargar productos"
                    )
                }
            )
        }
    }

    /**
     * Carga productos desde la base de datos local (Room)
     */
    fun loadProductsLocal() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            repository.getAllProductsLocal().collect { products ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    products = products,
                    error = null
                )
            }
        }
    }

    /**
     * Refresca productos desde la API y los guarda localmente
     */
    fun refreshProductsFromApi() {
        _uiState.value = _uiState.value.copy(
            isLoadingFromApi = true,
            error = null
        )

        viewModelScope.launch {
            val result = repository.fetchProductsFromApi()

            _uiState.value = result.fold(
                onSuccess = { products ->
                    _uiState.value.copy(
                        isLoadingFromApi = false,
                        products = products,
                        successMessage = "Productos actualizados",
                        error = null
                    )
                },
                onFailure = { exception ->
                    // Si falla, mantener productos locales
                    _uiState.value.copy(
                        isLoadingFromApi = false,
                        error = "No se pudo actualizar: ${exception.message}"
                    )
                }
            )
        }
    }

    /**
     * Filtra productos por categoría (local)
     */
    fun filterByCategory(category: String) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            if (category == "Todos") {
                repository.getAllProductsLocal().collect { products ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        products = products
                    )
                }
            } else {
                repository.getProductsByCategoryLocal(category).collect { products ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        products = products
                    )
                }
            }
        }
    }

    /**
     * Busca productos por categoría en la API
     */
    fun searchByCategoriaApi(categoriaId: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            val result = repository.fetchProductsByCategoria(categoriaId)

            _uiState.value = result.fold(
                onSuccess = { products ->
                    _uiState.value.copy(
                        isLoading = false,
                        products = products,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al buscar productos"
                    )
                }
            )
        }
    }

    /**
     * Filtra productos con parámetros avanzados (API)
     */
    fun filtrarProductos(
        talla: String? = null,
        material: String? = null,
        estilo: String? = null,
        genero: String? = null,
        precioMin: Double? = null,
        precioMax: Double? = null
    ) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            val result = repository.filtrarProductos(
                talla = talla,
                material = material,
                estilo = estilo,
                genero = genero,
                precioMin = precioMin,
                precioMax = precioMax
            )

            _uiState.value = result.fold(
                onSuccess = { products ->
                    _uiState.value.copy(
                        isLoading = false,
                        products = products,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al filtrar productos"
                    )
                }
            )
        }
    }

    /**
     * Obtiene productos en tendencia desde la API
     */
    fun getTrendingProducts() {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            val result = repository.getTrendingProducts()

            _uiState.value = result.fold(
                onSuccess = { products ->
                    _uiState.value.copy(
                        isLoading = false,
                        products = products,
                        successMessage = "Productos en tendencia cargados",
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al cargar tendencias"
                    )
                }
            )
        }
    }

    /**
     * Obtiene un producto específico por ID (local)
     */
    fun getProductById(id: Int) {
        viewModelScope.launch {
            val result = repository.getProductByIdLocal(id)

            result.fold(
                onSuccess = { product ->
                    // Podrías agregar un estado para producto individual si lo necesitas
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Producto encontrado: ${product.name}"
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message
                    )
                }
            )
        }
    }

    /**
     * Limpia mensajes de éxito y error
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }

    /**
     * Resetea el estado a valores iniciales
     */
    fun resetState() {
        _uiState.value = ProductUiState()
        loadProductsWithCacheFirst()
    }
}