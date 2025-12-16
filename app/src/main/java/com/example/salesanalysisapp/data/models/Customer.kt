package com.example.salesanalysisapp.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,             // ID клиента
    val name: String,            // Название компании/ФИО
    val contactPerson: String?,  // Контактное лицо
    val phone: String?,          // Телефон
    val address: String?         // Адрес
)