package com.example.salesanalysisapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Store
import androidx.compose.ui.graphics.vector.ImageVector


/**
 * Класс-описание для элемента навигационной панели.
 */
sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    // 1. Товары (Products) - с импортом прайс-листа
    object Products : BottomNavItem(NavRoute.PRODUCTS, Icons.Filled.Store, "Товары")

    // 2. Клиенты (Customers) - с CRUD операциями
    object Customers : BottomNavItem(NavRoute.CUSTOMERS, Icons.Filled.People, "Клиенты")

    // 3. Заказы (Orders) - основная функция учета
    object Orders : BottomNavItem(NavRoute.ORDERS, Icons.AutoMirrored.Filled.ReceiptLong, "Заказы")

    // 4. Аналитика (Analytics) - ключевой раздел курсовой
    object Analytics : BottomNavItem(NavRoute.ANALYTICS, Icons.Filled.Analytics, "Аналитика")
}