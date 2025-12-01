package com.example.modaurbana.ui.navigation

sealed class Route(val route: String) {
    object Login : Route("login")
    object Register : Route("register")
    object Main : Route("main")

    object Profile : Route("profile")
    object Home : Route("home")

    object ProductList : Route("product_list")

    object Cart : Route("cart")
}