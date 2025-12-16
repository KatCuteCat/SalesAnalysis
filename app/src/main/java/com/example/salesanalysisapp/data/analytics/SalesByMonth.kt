package com.example.salesanalysisapp.data.analytics

/**
 * Объект для хранения суммы продаж за месяц.
 */
data class SalesByMonth(
    val yearMonth: String, // Формат: ГГГГ-ММ (например, "2023-11")
    val totalSales: Double // Общая сумма продаж за этот месяц
)