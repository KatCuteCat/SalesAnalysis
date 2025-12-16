package com.example.salesanalysisapp.data.dao

import androidx.room.*
import com.example.salesanalysisapp.data.models.Customer
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    // ЧТЕНИЕ: Получить всех клиентов, сортировка по имени
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    // ЧТЕНИЕ: Получить клиента по ID
    @Query("SELECT * FROM customers WHERE id = :customerId")
    suspend fun getCustomerById(customerId: Int): Customer?

    // СОЗДАНИЕ/ОБНОВЛЕНИЕ: Вставить или обновить клиента
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: Customer): Long // Long - возвращает ID вставленной строки

    // УДАЛЕНИЕ: Удалить клиента
    @Delete
    suspend fun delete(customer: Customer)

    @Query("DELETE FROM customers") // или DELETE FROM products
    suspend fun deleteAll()
}