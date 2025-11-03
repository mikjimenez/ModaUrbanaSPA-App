package com.example.modaurbana.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.modaurbana.app.data.local.SessionManager
import com.example.modaurbana.app.ui.screens.*
import com.example.modaurbana.app.viewmodel.AuthViewModel
import com.example.modaurbana.app.viewmodel.CartViewModel
import com.example.modaurbana.app.viewmodel.ProductViewModel
import com.example.modaurbana.app.viewmodel.ProfileViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// Rutas de navegación
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
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

    // Verificar si hay sesión activa
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val isLoggedIn = sessionManager.isLoggedIn().first()
        startDestination = if (isLoggedIn) {
            Screen.Home.route
        } else {
            Screen.Login.route
        }
    }

    // Mostrar loading mientras se verifica la sesión
    if (startDestination == null) {
        // Puedes poner una splash screen aquí
        return
    }

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
        // PANTALLA DE LOGIN
        // ==========================================
        composable(Screen.Login.route) {
            val authViewModel: AuthViewModel = viewModel()

            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        // ==========================================
        // PANTALLA DE REGISTRO
        // ==========================================
        composable(Screen.Register.route) {
            val authViewModel: AuthViewModel = viewModel()

            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
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
            val productViewModel: ProductViewModel = viewModel()
            val cartViewModel: CartViewModel = viewModel()

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
            val cartViewModel: CartViewModel = viewModel()

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
            val profileViewModel: ProfileViewModel = viewModel()

            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    scope.launch {
                        sessionManager.clearSession()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                viewModel = profileViewModel
            )
        }
    }
}