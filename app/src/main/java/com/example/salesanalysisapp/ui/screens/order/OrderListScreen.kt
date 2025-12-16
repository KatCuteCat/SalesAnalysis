package com.example.salesanalysisapp.ui.screens.order

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.salesanalysisapp.data.relations.OrderWithCustomer
import com.example.salesanalysisapp.ui.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(
    navController: NavController, // Пока не используется, но пригодится для навигации к созданию
    viewModel: OrderViewModel = viewModel()
) {
    // Получаем список заказов с клиентами
    val orders by viewModel.allOrdersWithCustomer.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Заказы (${orders.size})") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("order_create") // <--- ИЗМЕНЕНИЕ
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Создать заказ")
            }
        }
    ) { paddingValues ->
        if (orders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Заказы отсутствуют. Создайте новый заказ.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                items(orders, key = { it.order.id }) { orderWithCustomer ->
                    OrderItemRow(orderWithCustomer = orderWithCustomer, onClick = {
                        navController.navigate("order_details/${orderWithCustomer.order.id}")
                    })
                    HorizontalDivider()
                }
            }
        }
    }
}

// =================================================================================
// КОМПОНЕНТ ОТОБРАЖЕНИЯ ОДНОГО ЗАКАЗА
// =================================================================================

@Composable
fun OrderItemRow(orderWithCustomer: OrderWithCustomer, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    val order = orderWithCustomer.order
    val customer = orderWithCustomer.customer

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            // Номер и дата заказа
            Text("Заказ № ${order.id}", style = MaterialTheme.typography.titleMedium)

            // Клиент
            Text("Клиент: ${customer.name}", style = MaterialTheme.typography.bodyMedium)

            // Дата
            Text(
                "Дата: ${dateFormat.format(Date(order.date))}",
                style = MaterialTheme.typography.bodySmall
            )
        }
        // Статус заказа
        Text(
            order.status,
            color = if (order.status == "Выполнен") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleSmall
        )
    }
}
