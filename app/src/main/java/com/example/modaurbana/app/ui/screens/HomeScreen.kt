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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.modaurbana.app.data.remote.dto.ProductoDto
import com.example.modaurbana.app.viewmodel.CartViewModel
import com.example.modaurbana.app.viewmodel.ProductViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCart: () -> Unit,
    onNavigateToProfile: () -> Unit,
    productViewModel: ProductViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel()
) {
    val productState by productViewModel.uiState.collectAsState()

    val cartState by cartViewModel.uiState.collectAsState()

    val categories = listOf("Todos", "Polera", "Pantalón", "Zapatilla", "Chaqueta")

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(cartState.successMessage) {
        cartState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            cartViewModel.clearMessages()
        }
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
            // Filtros de categoría
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = productState == category ||
                                (category == "Todos" && productState.selectedCategoria == null),
                        onClick = { productViewModel.filterByCategory(category) },
                        label = { Text(category) },
                        leadingIcon = if (productState.selectedCategoria == category ||
                            (category == "Todos" && productState.selectedCategoria == null)) {
                            {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else null
                    )
                }
            }

            // Lista de productos
            if (productState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (productState.productos.isEmpty()) {
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
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { productViewModel.refreshProductos() }) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Recargar")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = productState.productos,
                        key = { it.id }
                    ) { producto ->
                        ProductCard(
                            producto = producto,
                            onAddToCart = {
                                cartViewModel.addToCart(
                                    productoId = producto.id,
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
    producto: Producto,
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
        Column(modifier = Modifier.padding(16.dp)) {
            // Imagen del producto
            producto.imagen?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = producto.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = producto.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        producto.categoria?.nombre?.let { categoriaNombre ->
                            AssistChip(
                                onClick = { },
                                label = { Text(categoriaNombre) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Category,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.height(28.dp)
                            )
                        }

                        producto.talla?.let { talla ->
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Talla: $talla",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format("%,.0f", producto.precio)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    producto.stock?.let { stock ->
                        Text(
                            text = "Stock: $stock",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (stock > 10) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    producto.material?.let { material ->
                        Text(
                            text = "Material:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = material,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    producto.estilo?.let { estilo ->
                        Text(
                            text = "Estilo:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = estilo,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    producto.color?.let { color ->
                        Text(
                            text = "Color:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
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
                enabled = (producto.stock ?: 0) > 0
            ) {
                Icon(Icons.Default.AddShoppingCart, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if ((producto.stock ?: 0) > 0) "Agregar al Carrito" else "Sin Stock")
            }
        }
    }
}