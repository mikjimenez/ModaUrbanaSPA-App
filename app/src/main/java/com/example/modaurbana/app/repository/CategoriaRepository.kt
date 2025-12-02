package com.example.modaurbana.app.repository

import android.content.Context
import com.example.modaurbana.app.data.remote.RetrofitClient
import com.example.modaurbana.app.data.remote.dto.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * Repositorio para manejar categorías
 */
class CategoriaRepository(context: Context) {

    private val apiService = RetrofitClient.ApiService

    /**
     * Obtiene todas las categorías
     */
    suspend fun getCategorias(): Result<List<Categoria>> {
        return try {
            val response = apiService!!.getCategorias()

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Error al obtener categorías"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Obtiene una categoría por ID
     */
    suspend fun getCategoriaById(id: String): Result<Categoria> {
        return try {
            val response = apiService!!.getCategoriaById(id)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Categoría no encontrada"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Crea una nueva categoría (solo ADMIN)
     */
    suspend fun createCategoria(
        nombre: String,
        descripcion: String? = null,
        imagen: String? = null
    ): Result<Categoria> {
        return try {
            val request = CreateCategoriaRequest(
                nombre = nombre,
                descripcion = descripcion,
                imagen = imagen
            )

            val response = apiService!!.createCategoria(request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                val errorMessage = when (response.code()) {
                    403 -> "No tienes permisos para crear categorías"
                    400 -> "Datos inválidos"
                    else -> "Error al crear categoría"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Actualiza una categoría existente
     */
    suspend fun updateCategoria(
        id: String,
        nombre: String? = null,
        descripcion: String? = null,
        imagen: String? = null
    ): Result<Categoria> {
        return try {
            val request = UpdateCategoriaRequest(
                nombre = nombre,
                descripcion = descripcion,
                imagen = imagen
            )

            val response = apiService!!.updateCategoria(id, request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Error al actualizar categoría"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Elimina una categoría
     */
    suspend fun deleteCategoria(id: String): Result<String> {
        return try {
            val response = apiService!!.deleteCategoria(id)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                Result.failure(Exception("Error al eliminar categoría"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Sube una imagen para una categoría
     */
    suspend fun uploadCategoriaImage(
        categoriaId: String,
        imageFile: File
    ): Result<UploadImageData> {
        return try {
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)

            val response = apiService!!.uploadCategoriaImage(categoriaId, body)

            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Error al procesar imagen"))
                }
            } else {
                Result.failure(Exception("Error al subir imagen"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }
}