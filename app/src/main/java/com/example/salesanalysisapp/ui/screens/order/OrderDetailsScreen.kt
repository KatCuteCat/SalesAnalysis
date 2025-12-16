package com.example.salesanalysisapp.ui.screens.order

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.salesanalysisapp.data.relations.OrderWithItemsAndCustomer
import com.example.salesanalysisapp.ui.viewmodel.OrderViewModel
import com.example.salesanalysisapp.data.AppDatabase
import com.example.salesanalysisapp.data.utils.InvoiceFormatter
import com.example.salesanalysisapp.data.models.OrderItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Intent

val ORDER_STATUSES = listOf("Новый", "В обработке", "Отправлен", "Выполнен", "Отменен")

// НОВЫЙ DATA CLASS ДЛЯ КОМПЛЕКСНОГО ОТОБРАЖЕНИЯ (Оставляем здесь)
data class OrderItemDisplay(
    val item: OrderItem,
    val displayName: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    navController: NavController,
    orderId: Int,
    viewModel: OrderViewModel = viewModel()
) {
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    var fullOrder by remember { mutableStateOf<OrderWithItemsAndCustomer?>(null) }
    var currentStatus by remember { mutableStateOf("") }
    var isStatusMenuExpanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    val productDao = remember { AppDatabase.getDatabase(context).productDao() }

    // Карта для отображения, где ключ - OrderItem.id, значение - OrderItemDisplay
    val displayedItems = remember { mutableStateListOf<OrderItemDisplay>() }

    LaunchedEffect(orderId) {
        fullOrder = viewModel.getFullOrderById(orderId)
        currentStatus = fullOrder?.order?.status ?: ""

        displayedItems.clear()

        fullOrder?.items?.forEach { item ->
            // Используем productNameAtSale, которое мы сохранили при создании заказа
            val storedName = item.productNameAtSale

            // Проверяем, существует ли товар сейчас
            val currentProduct = productDao.getProductById(item.productId)

            val displayName = if (currentProduct == null) {
                // Товар удален
                "$storedName (Товар удален)"
            } else if (currentProduct.name != storedName) {
                // Товар был переименован
                "$storedName (Переименован в: ${currentProduct.name})"
            } else {
                // Товар существует и название не менялось
                storedName
            }

            displayedItems.add(OrderItemDisplay(item, displayName))
        }
    }

    val totalAmount = fullOrder?.items?.sumOf { it.quantity * it.priceAtSale } ?: 0.0

    val changeStatus: (String) -> Unit = { newStatus ->
        coroutineScope.launch {
            viewModel.updateOrderStatus(orderId, newStatus)
            currentStatus = newStatus
            isStatusMenuExpanded = false
            // Перезагрузка данных для обновления экрана
            fullOrder = viewModel.getFullOrderById(orderId)
        }
    }

    val exportInvoice: (OrderWithItemsAndCustomer, List<OrderItemDisplay>) -> Unit = { orderData, displayList ->
        // InvoiceFormatter использует List<OrderItemDisplay>, чтобы получить актуальное имя (включая пометку "удален")
        val invoiceText = InvoiceFormatter.formatInvoice(orderData, displayList)

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, invoiceText)
            type = "text/plain"
        }

        context.startActivity(Intent.createChooser(shareIntent, "Экспортировать счет №$orderId"))
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Заказ №${orderId}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (fullOrder != null && displayedItems.isNotEmpty()) {
                        TextButton(onClick = {
                            exportInvoice(fullOrder!!, displayedItems)
                        }) {
                            Text("Экспорт")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->

        val orderData = fullOrder
        if (orderData == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Заказ не найден.")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // === 1. Основная информация и общая сумма ===
            OrderInfoCard(orderData, totalAmount, dateFormat)

            Spacer(Modifier.height(16.dp))

            // === 2. Управление статусом ===
            StatusControl(
                currentStatus = currentStatus,
                isExpanded = isStatusMenuExpanded,
                // ИСПРАВЛЕНИЕ 'it': используем лямбду с явным именем параметра
                onExpandedChange = { expanded -> isStatusMenuExpanded = expanded },
                onStatusSelected = changeStatus
            )

            Spacer(Modifier.height(16.dp))

            // === 3. Детализация заказа (Товары) ===
            Text("Детализация заказа:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            OrderItemsTable(displayedItems)

            Spacer(Modifier.height(16.dp))
        }
    }
}


// =================================================================================
// Вспомогательные Composable (РАЗРЕШЕНИЕ ОШИБОК Unresolved reference)
// =================================================================================

@Composable
fun OrderInfoCard(orderData: OrderWithItemsAndCustomer, totalAmount: Double, dateFormat: SimpleDateFormat) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Клиент: ${orderData.customer.name}",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Дата: ${dateFormat.format(Date(orderData.order.date))}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Общая сумма: ${String.format(Locale.getDefault(), "%.2f ₽", totalAmount)}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusControl(
    currentStatus: String,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onStatusSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Текущий статус:", style = MaterialTheme.typography.titleMedium)

        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = onExpandedChange
        ) {
            OutlinedTextField(
                value = currentStatus,
                onValueChange = {},
                readOnly = true,
                label = { Text("Статус") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                // Используем рекомендованный overload menuAnchor()
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    .width(200.dp)
            )

            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                ORDER_STATUSES.forEach { status ->
                    DropdownMenuItem(
                        text = { Text(status) },
                        onClick = { onStatusSelected(status) },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

@Composable
fun OrderItemsTable(displayedItems: List<OrderItemDisplay>) {

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(8.dp)) {
            // Заголовок таблицы
            Row(Modifier.fillMaxWidth()) {
                // Увеличенный вес для названия товара, чтобы вместить пометку "удален"
                Text("Товар", Modifier.weight(2.5f), style = MaterialTheme.typography.titleSmall)
                Text("Цена", Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.End)
                Text("Кол-во", Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.End)
                Text("Сумма", Modifier.weight(1.5f), style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.End)
            }
            HorizontalDivider(Modifier.padding(vertical = 4.dp))

            displayedItems.forEach { displayItem ->
                val item = displayItem.item

                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    // Используем DisplayName, который включает пометку "удален"
                    Text(displayItem.displayName, Modifier.weight(2.5f), style = MaterialTheme.typography.bodyMedium)

                    Text(
                        String.format(Locale.getDefault(), "%.2f", item.priceAtSale),
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End
                    )
                    Text(
                        item.quantity.toString(),
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End
                    )
                    Text(
                        String.format(Locale.getDefault(), "%.2f", item.quantity * item.priceAtSale),
                        Modifier.weight(1.5f),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}