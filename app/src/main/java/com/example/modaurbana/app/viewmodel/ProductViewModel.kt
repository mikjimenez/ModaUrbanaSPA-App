package com.example.modaurbana.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.modaurbana.app.data.local.SessionManager
import com.example.modaurbana.app.data.remote.dto.ProductoDto
import com.example.modaurbana.app.models.Producto
import com.example.modaurbana.app.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ProductRepository(
        SessionManager(app.applicationContext)
    )

    private val _ui = MutableStateFlow(ProductListUiState())
    val ui: StateFlow<ProductListUiState> = _ui

    init {
        loadProductos()
    }

    fun loadProductos() {
        viewModelScope.launch {
            try {
                _ui.value = _ui.value.copy(
                    isLoading = true,
                    error = null
                )

                val productos = repo.getProductos()
                println(productos)
                _ui.value = _ui.value.copy(
                    isLoading = false,
                    productos = productos,
                    tallasDisponibles = productos.mapNotNull { it.talla }.distinct(),
                    materialesDisponibles = productos.mapNotNull { it.material }.distinct(),
                    estilosDisponibles = productos.mapNotNull { it.estilo }.distinct()
                )
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar productos"
                )
            }
        }
    }

    fun aplicarFiltros(
        talla: String?,
        material: String?,
        estilo: String?
    ) {
        val current = _ui.value
        _ui.value = current.copy(
            tallaSeleccionada = talla,
            materialSeleccionado = material,
            estiloSeleccionado = estilo
        )
    }
}

data class ProductListUiState(
    val isLoading: Boolean = false,
    val productos: List<ProductoDto> = emptyList(),
    val error: String? = null,

    val tallasDisponibles: List<String> = emptyList(),
    val materialesDisponibles: List<String> = emptyList(),
    val estilosDisponibles: List<String> = emptyList(),


    val tallaSeleccionada: String? = null,
    val materialSeleccionado: String? = null,
    val estiloSeleccionado: String? = null
) {
    val productosFiltrados: List<ProductoDto>
        get() = productos.filter { p ->
            (tallaSeleccionada == null || p.talla == tallaSeleccionada) &&
                    (materialSeleccionado == null || p.material == materialSeleccionado) &&
                    (estiloSeleccionado == null || p.estilo == estiloSeleccionado)
        }
}
