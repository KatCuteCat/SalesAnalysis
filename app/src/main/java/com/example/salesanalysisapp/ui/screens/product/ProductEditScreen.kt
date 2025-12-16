package com.example.salesanalysisapp.ui.screens.product

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.salesanalysisapp.data.models.Product
import com.example.salesanalysisapp.ui.viewmodel.ProductViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditScreen(
    navController: NavController,
    productId: Int, // 0 для создания, > 0 для редактирования
    viewModel: ProductViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 1. ПОЛЯ СОСТОЯНИЯ ФОРМЫ (Mutables)
    var name by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    // Состояние для хранения исходного объекта (нужно для удаления, где нужен ID)
    var currentProduct by remember { mutableStateOf<Product?>(null) }


    // 2. ЗАГРУЗКА ДАННЫХ (ТОЛЬКО ДЛЯ РЕЖИМА РЕДАКТИРОВАНИЯ)
    LaunchedEffect(productId) {
        if (productId != 0) {
            scope.launch {
                val product = viewModel.getProductById(productId)
                currentProduct = product
                if (product != null) {
                    // ИСПРАВЛЕНИЕ: Используем .orEmpty() для безопасного присвоения nullable-строк
                    name = product.name.orEmpty()
                    unitPrice = product.unitPrice.toString()
                    unit = product.unit.orEmpty()
                    category = product.category.orEmpty()
                } else {
                    Toast.makeText(context, "Товар с ID $productId не найден", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            }
        }
    }

    // 3. ФУНКЦИИ ОБРАБОТКИ
    val onSave: () -> Unit = FloatingActionButton@{
        // Базовая валидация
        if (name.isBlank() || unitPrice.isBlank() || unit.isBlank()) {
            Toast.makeText(context, "Заполните все обязательные поля.", Toast.LENGTH_SHORT).show()
            return@FloatingActionButton
        }

        val price = unitPrice.toDoubleOrNull()
        if (price == null || price <= 0) {
            Toast.makeText(context, "Цена должна быть положительным числом.", Toast.LENGTH_SHORT).show()
            return@FloatingActionButton
        }

        val productToSave = Product(
            // Если currentProduct не null, используем его ID, иначе Room сгенерирует новый (0)
            id = currentProduct?.id ?: 0,
            name = name.trim(),
            unitPrice = price,
            unit = unit.trim(),
            category = category.trim().ifBlank { "" } // Если категория пуста, сохраняем как null
        )

        viewModel.saveProduct(productToSave)
        Toast.makeText(context, "Товар сохранен.", Toast.LENGTH_SHORT).show()
        navController.popBackStack() // Возвращаемся к списку
    }

    val onDelete: () -> Unit = {
        currentProduct?.let { product ->
            viewModel.deleteProduct(product)
            Toast.makeText(context, "Товар '${product.name}' удален.", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    // 4. UI ИНТЕРФЕЙС
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (productId == 0) "Создание товара" else "Редактирование товара")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    // Кнопка удаления (только в режиме редактирования)
                    if (productId != 0 && currentProduct != null) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Filled.Delete, contentDescription = "Удалить товар")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onSave) {
                Text(if (productId == 0) "СОХРАНИТЬ" else "ОБНОВИТЬ", modifier = Modifier.padding(16.dp))
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()), // Скроллинг для маленьких экранов
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Spacer(modifier = Modifier.height(8.dp)) // Небольшой отступ сверху

            // Поле 1: Название товара
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название товара*") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Поле 2: Цена
            OutlinedTextField(
                value = unitPrice,
                onValueChange = { unitPrice = it.replace(",", ".") }, // Замена запятой на точку
                label = { Text("Цена за единицу*") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Поле 3: Единица измерения
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Ед. измерения*") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                // Поле 4: Категория
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Категория (опц.)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(72.dp)) // Отступ для FloatingActionButton
        }
    }
}