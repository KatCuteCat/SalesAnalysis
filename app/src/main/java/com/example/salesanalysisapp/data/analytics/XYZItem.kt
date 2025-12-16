package com.example.salesanalysisapp.data.analytics

/**
 * Объект для хранения результата XYZ-анализа по товару.
 */
data class XYZItem(
    val productId: Int,
    val productName: String,
    val avgMonthlySales: Double,      // Среднемесячный объем продаж
    val coefficientOfVariation: Double, // Коэффициент вариации (CV, в %)
    val xyzGroup: String              // Группа (X, Y или Z)
)