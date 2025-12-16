package com.example.salesanalysisapp.data.dao

import androidx.room.*
import com.example.salesanalysisapp.data.models.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    // ИСПРАВЛЕНИЕ: Универсальная функция для поиска
    // В запросе используется:
    // 1. LIKE '%' || :searchQuery || '%' — для поиска подстроки
    // 2. OR — для поиска по названию ИЛИ по категории
    @Query("""
        SELECT * FROM products 
        WHERE name LIKE '%' || :searchQuery || '%' 
        OR category LIKE '%' || :searchQuery || '%'
        ORDER BY name ASC
    """)
    fun getProductsBySearch(searchQuery: String): Flow<List<Product>> // <-- Новое имя функции

    // Удаляем старую функцию getAllProducts(), так как getProductsBySearch ее заменяет.
    // Если вам нужна getAllProducts, то она должна быть такой:
    // @Query("SELECT * FROM products ORDER BY name ASC")
    // fun getAllProducts(): Flow<List<Product>>
    // НО для поиска лучше оставить только getProductsBySearch и вызывать ее с пустой строкой.

    // ЧТЕНИЕ (Read): Получить один товар по ID
    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: Int): Product?

    // СОЗДАНИЕ/ОБНОВЛЕНИЕ (Create/Update): Вставить или обновить товар
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product)

    // УДАЛЕНИЕ (Delete): Удалить товар
    @Delete
    suspend fun delete(product: Product)

    // УДАЛЕНИЕ (Delete): Удалить все товары
    @Query("DELETE FROM products")
    suspend fun deleteAll()

    @Query("SELECT id FROM products WHERE name = :productName")
    suspend fun getProductIdByName(productName: String): Int?

    @Query("DELETE FROM products WHERE id IN (:productIds)")
    suspend fun deleteProductsByIds(productIds: List<Int>)
}