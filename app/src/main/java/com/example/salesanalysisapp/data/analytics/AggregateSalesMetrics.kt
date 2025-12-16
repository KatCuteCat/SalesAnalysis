package com.example.salesanalysisapp.data.analytics

//Объект для хранения общей выручки и общего количества заказов.

data class AggregateSalesMetrics(
    val totalRevenue: Double,
    val totalOrders: Int
) {
    // Вычисляемое свойство: Средний чек
    val averageCheck: Double
        get() = if (totalOrders > 0) totalRevenue / totalOrders else 0.0
}