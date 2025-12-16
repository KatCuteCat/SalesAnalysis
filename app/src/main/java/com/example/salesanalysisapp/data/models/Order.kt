package com.example.salesanalysisapp.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,             // ID заказа
    val customerId: Int,         // ID клиента (связь)
    val date: Long = System.currentTimeMillis(), // Дата и время создания (в виде timestamp)
    val status: String,          // Статус ("Новый", "Выполнен" и т.д.)
    val managerId: Int = 1       // ID менеджера (для фильтрации "свои заказы")
)