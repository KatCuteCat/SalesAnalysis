package com.example.salesanalysisapp.data.analytics

/**
 * Объект для хранения информации о товаре в ТОП-листе.
 */
data class TopProduct(
    val productName: String,
    val quantitySold: Int
)