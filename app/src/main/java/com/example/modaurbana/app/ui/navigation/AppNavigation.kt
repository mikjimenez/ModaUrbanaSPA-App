package com.example.modaurbana.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.modaurbana.app.data.local.SessionManager
import com.example.modaurbana.app.ui.screens.*
import com.example.modaurbana.app.viewmodel.AuthViewModel
import com.example.modaurbana.app.viewmodel.CartViewModel
import com.example.modaurbana.app.viewmodel.ProductViewModel
import com.example.modaurbana.app.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

// Rutas de navegación

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object ProductList : Screen("product_list")

    object Cart : Screen("cart")
    object Profile : Screen("profile")
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation(
    sessionManager: SessionManager
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Estado para manejar la verificación de sesión
    var isCheckingSession by remember { mutableStateOf(true) }
    var startDestination by remember { mutableStateOf<String?>(null) }

    // Verificar si hay sesión activa al iniciar
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val isLoggedIn = sessionManager.isLoggedIn()
                startDestination = if (isLoggedIn) {
                    Screen.Home.route
                } else {
                    Screen.Login.route
                }
            } catch (e: Exception) {
                // En caso de error, ir al login por defecto
                startDestination = Screen.Login.route
            } finally {
                isCheckingSession = false
            }
        }
    }

    // Mostrar pantalla de carga mientras se verifica la sesión
    if (isCheckingSession || startDestination == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Navegación principal
    NavHost(
        navController = navController,
        startDestination = startDestination!!,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) +
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) +
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300)
                    )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) +
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(300)
                    )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) +
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    )
        }
    ) {

        // ==========================================
        // PANTALLA DE REGISTRO
        // ==========================================
        composable(Screen.Register.route) {
            val authViewModel: AuthViewModel = viewModel(
                factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
                    .getInstance(context.applicationContext as android.app.Application)
            )

            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    // Navegar a Home y limpiar el backstack
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        // ==========================================
        // PANTALLA DE LOGIN
        // ==========================================
        composable(Screen.Login.route) {
            // Crear ViewModel específico para esta pantalla
            val authViewModel: AuthViewModel = viewModel(
                factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
                    .getInstance(context.applicationContext as android.app.Application)
            )

            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    // Navegar a Home y limpiar el backstack
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        // ==========================================
        // PANTALLA HOME (Catálogo)
        // ==========================================
        composable(Screen.Home.route) {
            val productViewModel: ProductViewModel = viewModel(
                factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
                    .getInstance(context.applicationContext as android.app.Application)
            )

            val cartViewModel: CartViewModel = viewModel(
                factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
                    .getInstance(context.applicationContext as android.app.Application)
            )

            HomeScreen(
                onNavigateToCart = {
                    navController.navigate(Screen.Cart.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                productViewModel = productViewModel,
                cartViewModel = cartViewModel
            )
        }

        // ==========================================
        // PANTALLA DEL CARRITO
        // ==========================================
        composable(Screen.Cart.route) {
            val cartViewModel: CartViewModel = viewModel(
                factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
                    .getInstance(context.applicationContext as android.app.Application)
            )

            CartScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                cartViewModel = cartViewModel
            )
        }

        // ==========================================
        // PANTALLA DE PERFIL
        // ==========================================
        composable(Screen.Profile.route) {
            val profileViewModel: ProfileViewModel = viewModel(
                factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
                    .getInstance(context.applicationContext as android.app.Application)
            )

            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    scope.launch {
                        try {
                            // Limpiar sesión
                            sessionManager.clearAllData()

                            // Navegar al login y limpiar todo el backstack
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        } catch (e: Exception) {
                            // Manejar error si es necesario
                            e.printStackTrace()
                        }
                    }
                },
                viewModel = profileViewModel
            )
        }
    }
}