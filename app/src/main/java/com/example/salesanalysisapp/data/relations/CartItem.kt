package com.example.salesanalysisapp.data.relations

import com.example.salesanalysisapp.data.models.Product

/**
 * Элемент, который пользователь добавляет в заказ (в корзину).
 * Содержит информацию о товаре и выбранном количестве.
 */
data class CartItem(
    val product: Product,
    val quantity: Int = 1 // Выбранное количество
) {
    // Вычисляемое свойство: Общая стоимость позиции
    val total: Double
        get() = product.unitPrice * quantity
}