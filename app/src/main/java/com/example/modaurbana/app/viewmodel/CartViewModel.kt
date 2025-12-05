package com.example.modaurbana.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.modaurbana.app.data.remote.dto.Carrito
import com.example.modaurbana.app.data.remote.dto.CarritoItem
import com.example.modaurbana.app.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CartUiState(
    val isLoading: Boolean = false,
    val carrito: Carrito? = null,
    val items: List<CarritoItem> = emptyList(),
    val total: Double = 0.0,
    val itemCount: Int = 0,
    val error: String? = null,
    val successMessage: String? = null
)

class CartViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CartRepository(application)

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState

    init {
        loadCarrito()
    }

    /**
     * Carga el carrito del usuario
     */
    fun loadCarrito() {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            val result = repository.getMyCarrito()

            _uiState.value = result.fold(
                onSuccess = { carrito ->
                    _uiState.value.copy(
                        isLoading = false,
                        carrito = carrito,
                        items = carrito.items ?: emptyList(),
                        total = carrito.total ?: 0.0,
                        itemCount = carrito.items?.size ?: 0,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al cargar carrito"
                    )
                }
            )
        }
    }

    /**
     * Agrega un producto al carrito
     */
    fun addToCart(
        producto: String,
        talla: String?,
        cantidad: Int = 1
    ) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            val result = repository.agregarItemCarrito(
                productoId = producto,
                talla = talla,
                cantidad = cantidad
            )

            _uiState.value = result.fold(
                onSuccess = { carrito ->
                    _uiState.value.copy(
                        isLoading = false,
                        carrito = carrito,
                        items = carrito.items ?: emptyList(),
                        total = carrito.total ?: 0.0,
                        itemCount = carrito.items?.size ?: 0,
                        successMessage = "Producto agregado al carrito",
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al agregar al carrito"
                    )
                }
            )
        }
    }

    /**
     * Remueve un item del carrito
     * Nota: Necesitarás el itemId del backend
     */
    fun removeItem(itemId: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            val result = repository.removerItemCarrito(itemId)

            result.fold(
                onSuccess = { message ->
                    // Recargar el carrito después de eliminar
                    loadCarrito()
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Producto eliminado del carrito"
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al eliminar producto"
                    )
                }
            )
        }
    }

    /**
     * Actualiza la cantidad de un producto
     * (Esto depende de cómo tu API maneje la actualización de cantidades)
     */
    fun updateQuantity(productoId: String, talla: String?, newQuantity: Int) {
        if (newQuantity <= 0) {
            // Si la cantidad es 0 o menos, podrías remover el item
            // Necesitarás encontrar el itemId correspondiente
            return
        }

        // Agregar el producto nuevamente con la nueva cantidad
        // (esto podría sumar a la cantidad existente, dependiendo de tu API)
        addToCart(productoId, talla, newQuantity)
    }

    /**
     * Confirma el pedido
     */
    fun checkout(
        direccionEntrega: String,
        metodoPago: String
    ) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            val result = repository.confirmarPedido(
                direccionEntrega = direccionEntrega,
                metodoPago = metodoPago
            )

            _uiState.value = result.fold(
                onSuccess = { pedido ->
                    _uiState.value.copy(
                        isLoading = false,
                        carrito = null,
                        items = emptyList(),
                        total = 0.0,
                        itemCount = 0,
                        successMessage = "¡Pedido realizado con éxito! ID: ${pedido.id}",
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al confirmar pedido"
                    )
                }
            )
        }
    }

    /**
     * Recarga el carrito (pull-to-refresh)
     */
    fun refreshCarrito() {
        loadCarrito()
    }

    /**
     * Calcula el total del carrito
     */
    fun calcularTotal() {
        viewModelScope.launch {
            val result = repository.calcularTotalCarrito()

            result.fold(
                onSuccess = { total ->
                    _uiState.value = _uiState.value.copy(
                        total = total
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
     * Obtiene la cantidad de items en el carrito
     */
    fun getItemCount() {
        viewModelScope.launch {
            val result = repository.getCartItemCount()

            result.fold(
                onSuccess = { count ->
                    _uiState.value = _uiState.value.copy(
                        itemCount = count
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
}