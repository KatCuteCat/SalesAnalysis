package com.example.salesanalysisapp.data.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.salesanalysisapp.data.models.Customer
import com.example.salesanalysisapp.data.models.Order

/**
 * Объединяет основную информацию о заказе (Order) и данные клиента (Customer).
 */
data class OrderWithCustomer(
    @Embedded
    val order: Order, // Основная информация о заказе

    @Relation(
        parentColumn = "customerId", // Колонка в таблице Order
        entityColumn = "id"          // Колонка в таблице Customer
    )
    val customer: Customer         // Соответствующий клиент
)