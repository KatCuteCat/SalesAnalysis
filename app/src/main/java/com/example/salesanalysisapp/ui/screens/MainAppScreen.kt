package com.example.salesanalysisapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.salesanalysisapp.ui.navigation.BottomNavItem
import com.example.salesanalysisapp.ui.navigation.NavRoute
import com.example.salesanalysisapp.ui.screens.analytics.AnalyticsScreen
import com.example.salesanalysisapp.ui.screens.customer.CustomerEditScreen
import com.example.salesanalysisapp.ui.screens.customer.CustomerListScreen
import com.example.salesanalysisapp.ui.screens.order.OrderCreateScreen
import com.example.salesanalysisapp.ui.screens.order.OrderDetailsScreen
import com.example.salesanalysisapp.ui.screens.order.OrderListScreen
import com.example.salesanalysisapp.ui.screens.product.ProductListScreen
import com.example.salesanalysisapp.ui.screens.product.ProductEditScreen // <--- УБЕДИТЕСЬ, ЧТО ЭТОТ ИМПОРТ ЕСТЬ

// Добавляем аннотацию OptIn для использования Experimental API, чтобы убрать предупреждение
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen() {

    val navController = rememberNavController()

    val navItems = listOf(
        BottomNavItem.Products,
        BottomNavItem.Customers,
        BottomNavItem.Orders,
        BottomNavItem.Analytics
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                navItems.forEach { item ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->

        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

// =================================================================================
// AppNavHost - Определяет все возможные экраны приложения.
// =================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavRoute.PRODUCTS,
        modifier = modifier
    ) {
        // 1. ЭКРАН ТОВАРОВ
        composable(NavRoute.PRODUCTS) {
            // <--- ИСПРАВЛЕНИЕ: ПЕРЕДАЕМ navController В ProductListScreen
            ProductListScreen(navController = navController)
        }

        // 2. ЭКРАН КЛИЕНТОВ
        composable(NavRoute.CUSTOMERS) {
            CustomerListScreen(navController = navController)
        }

        // 3. ЭКРАН ЗАКАЗОВ
        composable(NavRoute.ORDERS) {
            OrderListScreen(navController = navController)
        }

        // 4. ЭКРАН АНАЛИТИКИ
        composable(NavRoute.ANALYTICS) {
            AnalyticsScreen()
        }

        // 5. ЭКРАН СОЗДАНИЯ ЗАКАЗА
        composable("order_create") {
            OrderCreateScreen(navController = navController)
        }
        // 6. ЭКРАН ДЕТАЛЕЙ ЗАКАЗА
        composable("order_details/{orderId}") { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId")?.toIntOrNull() ?: 0
            if (orderId > 0) {
                OrderDetailsScreen(navController = navController, orderId = orderId)
            } else {
                PlaceholderScreen(title = "Ошибка", content = "Заказ не найден")
            }
        }

        // 7. Маршрут для редактирования клиента
        composable("customer_edit/{customerId}") { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId")?.toIntOrNull() ?: 0
            CustomerEditScreen(navController = navController, customerId = customerId)
        }

        // 8. Маршрут для создания/редактирования товара (ProductEditScreen)
        composable("product_edit/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")?.toIntOrNull() ?: 0
            // Вызываем ProductEditScreen, который мы создали как заглушку
            ProductEditScreen(navController = navController, productId = productId)
        }
    }
}

// =================================================================================
// PlaceholderScreen
// =================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(title: String, content: String) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(title) }) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(content, style = MaterialTheme.typography.headlineSmall)
        }
    }
}