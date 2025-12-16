package com.example.salesanalysisapp.data.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.salesanalysisapp.data.models.Customer
import com.example.salesanalysisapp.data.models.Order
import com.example.salesanalysisapp.data.models.OrderItem

/**
 * Полное представление заказа, включающее заголовок (Order),
 * список деталей (OrderItems) и данные клиента (Customer).
 */
data class OrderWithItemsAndCustomer(
    @Embedded
    val order: Order, // Основная информация о заказе

    @Relation(
        parentColumn = "customerId",
        entityColumn = "id"
    )
    val customer: Customer, // Связанный клиент

    @Relation(
        parentColumn = "id",        // ID заказа в таблице Order
        entityColumn = "orderId"    // orderId в таблице OrderItem
    )
    val items: List<OrderItem> // Детализация заказа
)