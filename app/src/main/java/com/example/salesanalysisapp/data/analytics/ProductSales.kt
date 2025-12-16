package com.example.salesanalysisapp.data.analytics

/**
 * Промежуточный DTO для общей выручки по каждому товару.
 */
data class ProductSales(
    val productId: Int,
    val productName: String,
    val totalRevenue: Double
)