package com.example.salesanalysisapp.data.repository

import com.example.salesanalysisapp.data.dao.CustomerDao
import com.example.salesanalysisapp.data.models.Customer
import kotlinx.coroutines.flow.Flow

class CustomerRepository(private val customerDao: CustomerDao) {

    // Чтение всех клиентов в виде потока (для реактивного UI)
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()

    // Вставка/Обновление клиента. Возвращает ID вставленной/обновленной строки.
    suspend fun insert(customer: Customer): Long {
        return customerDao.insert(customer)
    }

    // Удаление клиента
    suspend fun delete(customer: Customer) {
        customerDao.delete(customer)
    }

    // Получение клиента по ID
    suspend fun getCustomerById(id: Int): Customer? {
        return customerDao.getCustomerById(id)
    }

    suspend fun deleteAllCustomers() {
        customerDao.deleteAll()
    }
}