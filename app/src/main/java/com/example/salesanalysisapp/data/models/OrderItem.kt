package com.example.salesanalysisapp.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_items")
data class OrderItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,             // ID записи
    val orderId: Int,            // ID заказа (связь)
    val productId: Int,          // ID товара (связь)
    val quantity: Int,           // Количество проданного товара
    val priceAtSale: Double,     // Цена товара на момент продажи (важно для аналитики)


    val productNameAtSale: String
)