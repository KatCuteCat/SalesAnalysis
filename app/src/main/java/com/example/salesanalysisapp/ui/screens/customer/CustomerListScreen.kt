package com.example.salesanalysisapp.ui.screens.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.salesanalysisapp.data.models.Customer
import com.example.salesanalysisapp.ui.viewmodel.CustomerViewModel

// =================================================================================
// 1. КОМПОНЕНТ ЭКРАНА СПИСКА КЛИЕНТОВ
// =================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    navController: NavController,
    viewModel: CustomerViewModel = viewModel()
) {
    // Реактивно отслеживаем список клиентов из ViewModel
    val customers by viewModel.allCustomers.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Клиенты (${customers.size})") },
                // TODO: Здесь будет кнопка переключения на другие разделы (Товары, Заказы)
            )
        },
        // Кнопка для добавления нового клиента (CRUD)
        floatingActionButton = {
            FloatingActionButton(onClick = {
                // Навигация на экран создания/редактирования клиента
                navController.navigate("customer_edit/0") // 0 - означает создание нового
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить клиента")
            }
        }
    ) { paddingValues ->

        // 2. ОТОБРАЖЕНИЕ СПИСКА КЛИЕНТОВ
        if (customers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Список клиентов пуст.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues).fillMaxSize()
            ) {
                items(customers, key = { it.id }) { customer ->
                    CustomerItem(
                        customer = customer,
                        onClick = {
                            // Навигация на экран редактирования существующего клиента
                            navController.navigate("customer_edit/${customer.id}")
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

// =================================================================================
// 3. КОМПОНЕНТ ОТОБРАЖЕНИЯ ОДНОГО КЛИЕНТА
// =================================================================================

@Composable
fun CustomerItem(customer: Customer, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(customer.name, style = MaterialTheme.typography.titleMedium)
            customer.contactPerson?.let {
                Text("Контакт: $it", style = MaterialTheme.typography.bodySmall)
            }
        }
        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Детали клиента")
    }
}