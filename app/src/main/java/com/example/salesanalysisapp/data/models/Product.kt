package com.example.salesanalysisapp.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,             // ID товара
    val name: String,            // Наименование
    val unitPrice: Double,       // Цена за единицу
    val unit: String,            // Единица измерения (шт., кг)
    val category: String,        // Категория (для ABC/XYZ анализа)
    val sku: String? = null      // Артикул (для импорта)
)