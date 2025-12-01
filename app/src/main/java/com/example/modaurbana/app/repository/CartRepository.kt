package com.example.modaurbana.app.repository

import android.content.Context
import com.example.modaurbana.app.data.local.AppDatabase
import com.example.modaurbana.app.data.local.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow

class CartRepository(context: Context) {
    private val cartDao = AppDatabase.getDatabase(context).cartDao()

    fun getAllCartItems(): Flow<List<CartItemEntity>> {
        return cartDao.getAllCartItems()
    }

    fun getTotalPrice(): Flow<Double?> {
        return cartDao.getTotalPrice()
    }

    /**
     * Agrega un producto al carrito
     */
    suspend fun addToCart(
        productId: Int,
        productName: String,
        size: String,
        price: Double,
        imageUrl: String?
    ): Result<Unit> {
        return try {
            val cartItem = CartItemEntity(
                productId = productId,
                productName = productName,
                size = size,
                price = price,
                quantity = 1,
                imageUrl = imageUrl
            )
            cartDao.addToCart(cartItem)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza la cantidad de un item
     */
    suspend fun updateQuantity(item: CartItemEntity, newQuantity: Int): Result<Unit> {
        return try {
            if (newQuantity <= 0) {
                cartDao.removeFromCart(item)
            } else {
                val updatedItem = item.copy(quantity = newQuantity)
                cartDao.updateCartItem(updatedItem)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFromCart(item: CartItemEntity): Result<Unit> {
        return try {
            cartDao.removeFromCart(item)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun clearCart(): Result<Unit> {
        return try {
            cartDao.clearCart()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}