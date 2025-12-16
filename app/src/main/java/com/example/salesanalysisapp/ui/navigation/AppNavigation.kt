package com.example.salesanalysisapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.salesanalysisapp.ui.screens.customer.CustomerListScreen
import com.example.salesanalysisapp.ui.screens.product.ProductListScreen
import com.example.salesanalysisapp.ui.screens.product.ProductEditScreen // <--- НОВЫЙ ИМПОРТ

/**
 * Определения маршрутов (Routes) для навигации.
 */
object NavRoute {
    const val PRODUCTS = "products"
    const val CUSTOMERS = "customers"
    const val ORDERS = "orders"
    const val ANALYTICS = "analytics"
    // ИСПРАВЛЕНИЕ 1: Добавлен маршрут для создания/редактирования товара
    const val PRODUCT_EDIT = "product_edit/{productId}"
    // TODO: Здесь будут маршруты для создания/редактирования:
    // const val CUSTOMER_EDIT = "customer_edit/{customerId}"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoute.PRODUCTS // Стартовый экран - список товаров
    ) {
        // 1. Экран Списка Товаров (с импортом)
        composable(NavRoute.PRODUCTS) {
            ProductListScreen(
                navController = navController // <--- ИСПРАВЛЕНИЕ 2: Передаем NavController
            )
        }

        // 2. Экран Списка Клиентов
        composable(NavRoute.CUSTOMERS) {
            CustomerListScreen(
                navController = navController
            )
        }

        // 3. ЭКРАН СОЗДАНИЯ/РЕДАКТИРОВАНИЯ ТОВАРА (НОВЫЙ)
        composable(NavRoute.PRODUCT_EDIT) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")?.toIntOrNull() ?: 0
            // Вызываем новый экран, передавая ID (0 для создания, >0 для редактирования)
            ProductEditScreen(navController = navController, productId = productId)
        }

        // TODO: Здесь будут экраны для Заказов, Редактирования Клиента, Аналитики
    }
}