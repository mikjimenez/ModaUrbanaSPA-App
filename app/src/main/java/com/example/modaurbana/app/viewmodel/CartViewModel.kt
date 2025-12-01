package com.example.modaurbana.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.modaurbana.app.data.local.entity.CartItemEntity
import com.example.modaurbana.app.repository.CartRepositoryHybrid
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CartUiState(
    val isLoading: Boolean = false,
    val cartItems: List<CartItemEntity> = emptyList(),
    val totalPrice: Double = 0.0,
    val error: String? = null,
    val successMessage: String? = null
)

class CartViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CartRepositoryHybrid(application)

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState

    init {
        loadCart()
    }

    /**
     * Carga el carrito
     */
    private fun loadCart() {
        viewModelScope.launch {
            repository.getAllCartItems().collect { items ->
                _uiState.value = _uiState.value.copy(cartItems = items)
            }
        }

        viewModelScope.launch {
            repository.getTotalPrice().collect { total ->
                _uiState.value = _uiState.value.copy(totalPrice = total ?: 0.0)
            }
        }
    }

    /**
     * Agrega un producto al carrito
     */
    fun addToCart(
        productId: Int,
        productName: String,
        size: String,
        price: Double,
        imageUrl: String?
    ) {
        viewModelScope.launch {
            val result = repository.addToCart(productId, productName, size, price, imageUrl)

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Producto agregado al carrito"
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
     * Actualiza la cantidad de un item
     */
    fun updateQuantity(item: CartItemEntity, newQuantity: Int) {
        viewModelScope.launch {
            repository.updateQuantity(item, newQuantity)
        }
    }

    /**
     * Elimina un item del carrito
     */
    fun removeItem(item: CartItemEntity) {
        viewModelScope.launch {
            repository.removeFromCart(item)
        }
    }

    /**
     * Finaliza la compra (limpia el carrito)
     */
    fun checkout() {
        viewModelScope.launch {
            val result = repository.clearCart()

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "¡Compra realizada con éxito!"
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
     * Limpia los mensajes
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }
}