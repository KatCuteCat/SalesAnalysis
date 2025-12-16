package com.example.salesanalysisapp.ui.screens.order

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.salesanalysisapp.data.models.Order
import com.example.salesanalysisapp.data.models.OrderItem
import com.example.salesanalysisapp.data.relations.CartItem
import com.example.salesanalysisapp.data.models.Customer
import com.example.salesanalysisapp.data.models.Product
import com.example.salesanalysisapp.ui.viewmodel.OrderViewModel
import java.util.UUID
import java.util.Locale
import androidx.compose.material3.ExposedDropdownMenuAnchorType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderCreateScreen(
    navController: NavController,
    viewModel: OrderViewModel = viewModel()
) {
    // 1. Состояние для выбранного клиента
    var selectedCustomer: Customer? by remember { mutableStateOf(null) }

    // 2. Состояние для корзины (список CartItem)
    val cartItems = remember { mutableStateListOf<CartItem>() }

    // НОВОЕ СОСТОЯНИЕ: Управление видимостью диалога выбора товара
    var showProductDialog by remember { mutableStateOf(false) }

    // 3. Общая стоимость заказа
    val totalOrderAmount = cartItems.sumOf { it.total }

    // Получение списков клиентов и товаров из ViewModel (для выбора)
    val allCustomers by viewModel.allCustomers.collectAsState(initial = emptyList())
    val allProducts by viewModel.allProducts.collectAsState(initial = emptyList())

    // НОВАЯ ЛОГИКА: Добавление товара в корзину
    val onProductSelected: (Product) -> Unit = { product ->
        showProductDialog = false // Закрываем диалог

        // Проверяем, есть ли уже этот товар в корзине
        val existingItem = cartItems.find { it.product.id == product.id }

        if (existingItem != null) {
            // Если товар уже есть, увеличиваем его количество на 1
            val index = cartItems.indexOf(existingItem)
            cartItems[index] = existingItem.copy(quantity = existingItem.quantity + 1)
        } else {
            // Если товара нет, добавляем его с количеством 1
            cartItems.add(
                CartItem(
                    product = product,
                    quantity = 1
                )
            )
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Новый Заказ") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    if (selectedCustomer != null && cartItems.isNotEmpty()) {
                        // 4. ЛОГИКА СОХРАНЕНИЯ ЗАКАЗА
                        val newOrder = Order(
                            customerId = selectedCustomer!!.id,
                            status = "Новый" // Начальный статус
                        )

                        // Преобразование CartItem в OrderItem
                        val orderItems = cartItems.map { cartItem ->
                            OrderItem(
                                orderId = 0, // Будет заменено в репозитории
                                productId = cartItem.product.id,
                                quantity = cartItem.quantity,
                                priceAtSale = cartItem.product.unitPrice, // Используем текущую цену

                                // Сохраняем название товара (из предыдущего исправления)
                                productNameAtSale = cartItem.product.name
                            )
                        }

                        viewModel.createOrder(newOrder, orderItems)
                        navController.popBackStack() // Возврат к списку
                    }
                },
                enabled = selectedCustomer != null && cartItems.isNotEmpty(), // Кнопка активна, если есть клиент и товары
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // ИСПРАВЛЕНИЕ ЛОКАЛИ
                Text("Оформить заказ (${String.format(Locale.getDefault(), "%.2f", totalOrderAmount)})")
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // == 1. Выбор Клиента ==
            CustomerSelection(
                selectedCustomer = selectedCustomer,
                allCustomers = allCustomers,
                onCustomerSelected = { selectedCustomer = it }
            )

            HorizontalDivider()

            // == 2. Список выбранных товаров (Корзина) ==
            Text(
                "Корзина (${cartItems.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(cartItems, key = { it.product.id }) { item ->
                    CartItemRow(
                        item = item,
                        onQuantityChange = { newQty ->
                            val index = cartItems.indexOf(item)
                            if (index != -1 && newQty > 0) {
                                cartItems[index] = item.copy(
                                    quantity = newQty
                                )
                            }
                        },
                        onRemove = {
                            cartItems.remove(item)
                        }
                    )
                    HorizontalDivider()
                }

                // Добавление кнопки для выбора товара в конце корзины
                item {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            showProductDialog = true // Активируем диалог
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить товар")
                        Spacer(Modifier.width(8.dp))
                        Text("Добавить товар в заказ")
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

    // НОВЫЙ КОМПОНЕНТ: Диалог выбора товара
    if (showProductDialog) {
        ProductSelectionDialog(
            allProducts = allProducts,
            onProductSelected = onProductSelected,
            onDismiss = { showProductDialog = false }
        )
    }
}


// =================================================================================
// Вспомогательные Composable
// =================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSelection(
    selectedCustomer: Customer?,
    allCustomers: List<Customer>,
    onCustomerSelected: (Customer) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.padding(16.dp)
    ) {
        OutlinedTextField(
            value = selectedCustomer?.name ?: "Выберите клиента",
            onValueChange = {},
            readOnly = true,
            label = { Text("Клиент") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            allCustomers.forEach { customer ->
                DropdownMenuItem(
                    text = { Text(customer.name) },
                    onClick = {
                        onCustomerSelected(customer)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Store, contentDescription = null)
        Spacer(Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(item.product.name, style = MaterialTheme.typography.bodyLarge)

            // ИСПРАВЛЕНИЕ 1: Добавлены " руб / " для единообразия в корзине
            Text(
                "${String.format(Locale.getDefault(), "%.2f руб", item.product.unitPrice)} / ${item.product.unit}",
                style = MaterialTheme.typography.bodySmall
            )
            // ИСПРАВЛЕНИЕ ЛОКАЛИ
            Text(
                "Сумма: ${String.format(Locale.getDefault(), "%.2f", item.total)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Кнопки управления количеством
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = { onQuantityChange(item.quantity - 1) },
                enabled = item.quantity > 1,
                contentPadding = PaddingValues(4.dp),
                modifier = Modifier.size(32.dp)
            ) { Text("-") }

            Text(
                item.quantity.toString(),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Button(
                onClick = { onQuantityChange(item.quantity + 1) },
                contentPadding = PaddingValues(4.dp),
                modifier = Modifier.size(32.dp)
            ) { Text("+") }
        }

        Spacer(Modifier.width(16.dp))

        // Кнопка удаления
        IconButton(onClick = onRemove) {
            Icon(Icons.Filled.Delete, contentDescription = "Удалить")
        }
    }
}

// =================================================================================
// КОМПОНЕНТ: Диалог выбора товара
// =================================================================================

@Composable
fun ProductSelectionDialog(
    allProducts: List<Product>,
    onProductSelected: (Product) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите товар") },
        text = {
            // Список товаров для выбора
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                items(allProducts, key = { it.id }) { product ->
                    ListItem(
                        headlineContent = { Text(product.name) },
                        supportingContent = {
                            Text(
                                // ИСПРАВЛЕНИЕ 2: Добавлены " руб / " в диалоге выбора товара
                                "Цена: ${String.format(Locale.getDefault(), "%.2f руб", product.unitPrice)} / ${product.unit}"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onProductSelected(product) }
                    )
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}