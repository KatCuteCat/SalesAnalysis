package com.example.salesanalysisapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.salesanalysisapp.data.AppDatabase
import com.example.salesanalysisapp.data.models.Customer
import com.example.salesanalysisapp.data.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CustomerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CustomerRepository

    // Все клиенты
    val allCustomers: Flow<List<Customer>>

    init {
        val customerDao = AppDatabase.getDatabase(application).customerDao()
        repository = CustomerRepository(customerDao)
        allCustomers = repository.allCustomers
    }

    // Функция для создания или обновления клиента
    fun insertCustomer(customer: Customer) = viewModelScope.launch {
        repository.insert(customer)
    }

    // Функция для удаления клиента
    fun deleteCustomer(customer: Customer) = viewModelScope.launch {
        repository.delete(customer)
    }

    suspend fun getCustomerById(id: Int): Customer? {
        return repository.getCustomerById(id)
    }
}