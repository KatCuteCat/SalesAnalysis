package com.example.salesanalysisapp.data.analytics

/**
 * Объект для хранения результата ABC-анализа по товару.
 */
data class ABCItem(
    val productId: Int,
    val productName: String,
    val totalRevenue: Double,      // Общая выручка по товару
    val percentage: Double,        // Доля в общей выручке (в %)
    val cumulativePercentage: Double, // Накопительная доля (в %)
    val abcGroup: String           // Группа (A, B или C)
)