package com.example.salesanalysisapp.data.analytics

//Объект для сбора данных о продажах одного товара за один месяц.

data class ProductMonthlySale(
    val productId: Int,
    val yearMonth: String, // Месяц, ГГГГ-ММ
    val quantitySold: Int  // Количество проданного товара
)