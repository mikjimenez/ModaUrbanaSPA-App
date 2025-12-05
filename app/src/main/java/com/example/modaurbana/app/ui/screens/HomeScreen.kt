package com.example.modaurbana.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.modaurbana.app.data.remote.dto.ProductoDto
import com.example.modaurbana.app.viewmodel.CartViewModel
import com.example.modaurbana.app.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCart: () -> Unit,
    onNavigateToProfile: () -> Unit,
    productViewModel: ProductViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel()
) {
    val productState by productViewModel.ui.collectAsState()
    val cartState by cartViewModel.uiState.collectAsState()

    // Categorías basadas en tus productos
    val categories = listOf("Todos", "Poleras", "Hoodies", "Pantalones", "Zapatillas")

    // Snackbar para mensajes
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar mensajes de éxito del carrito
    LaunchedEffect(cartState.successMessage) {
        cartState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            cartViewModel.clearMessages()
        }
    }

    // Mostrar mensajes de error del carrito
    LaunchedEffect(cartState.error) {
        cartState.error?.let {
            snackbarHostState.showSnackbar("Error: $it")
            cartViewModel.clearMessages()
        }
    }

    // Cargar productos al inicio
    LaunchedEffect(Unit) {
        productViewModel.loadProductos()
        cartViewModel.loadCarrito()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("ModaUrbana", fontWeight = FontWeight.Bold)
                        Text(
                            "Moda Sostenible",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                actions = {
                    // Badge del carrito
                    BadgedBox(
                        badge = {
                            if (cartState.itemCount > 0) {
                                Badge {
                                    Text("${cartState.itemCount}")
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = onNavigateToCart) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                        }
                    }

                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // LOADER
            if (productState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            // ERROR
            else if (productState.error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Error: ${productState.error}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { productViewModel.loadProductos() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            // SIN PRODUCTOS
            else if (productState.productosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No hay productos disponibles",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            // LISTA DE PRODUCTOS
            else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = productState.productosFiltrados,
                        key = { it.id }
                    ) { producto ->
                        ProductCard(
                            producto = producto,
                            isAddingToCart = cartState.isLoading,
                            onAddToCart = {
                                cartViewModel.addToCart(
                                    producto = producto.id,
                                    talla = producto.talla,
                                    cantidad = 1
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    producto: ProductoDto,
    isAddingToCart: Boolean = false,
    onAddToCart: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .clickable { isExpanded = !isExpanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Imagen del producto
            AsyncImage(
                model = producto.imagen,
                contentDescription = producto.nombre,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop,
                onError = { error ->
                    println("Error cargando imagen: ${error.result.throwable}")
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = producto.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = { },
                            label = { Text(producto.categoria.nombre) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Category,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.height(28.dp)
                        )

                        Text(
                            text = "Talla: ${producto.talla}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "CLP $${"%,.0f".format(producto.precio ?: 0.0).replace(",", ".")}",                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Stock: ${producto.stock}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (producto.stock > 10)
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }
            }

            // Detalles expandibles
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Material:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = producto.material,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Estilo:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = producto.estilo,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Descripción:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = producto.descripcion,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    producto.color?.let { color ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Color:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = color,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onAddToCart,
                modifier = Modifier.fillMaxWidth(),
                enabled = producto.stock > 0 && !isAddingToCart
            ) {
                if (isAddingToCart) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (producto.stock > 0) "Agregar al Carrito" else "Sin Stock")
                }
            }
        }
    }
}