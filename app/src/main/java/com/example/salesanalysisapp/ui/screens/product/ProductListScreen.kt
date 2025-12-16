package com.example.salesanalysisapp.ui.screens.product

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.salesanalysisapp.data.models.Product
import com.example.salesanalysisapp.ui.viewmodel.ProductViewModel
import java.io.InputStream

// =================================================================================
// 1. КОМПОНЕНТ ЭКРАНА СПИСКА ТОВАРОВ
// =================================================================================

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class) // ОБНОВЛЕН
@Composable
fun ProductListScreen(
    navController: NavController,
    viewModel: ProductViewModel = viewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val products by viewModel.allProducts.collectAsState(initial = emptyList())
    val searchQuery by viewModel.searchQuery.collectAsState()

    // НОВЫЕ СОСТОЯНИЯ ДЛЯ МНОЖЕСТВЕННОГО ВЫБОРА
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsState()
    val selectedProductIds by viewModel.selectedProductIds.collectAsState()

    // 3. LAUNCHER ДЛЯ ВЫБОРА ФАЙЛА
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                try {
                    val inputStream: InputStream? = context.contentResolver.openInputStream(it)
                    if (inputStream != null) {
                        viewModel.importPriceList(inputStream)
                        Toast.makeText(context, "Импорт прайс-листа запущен.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Не удалось открыть файл.", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Ошибка при обработке файла: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }
    )

    Scaffold(
        topBar = {
            Column {
                if (isMultiSelectMode) {
                    // РЕЖИМ МНОЖЕСТВЕННОГО ВЫБОРА
                    MultiSelectTopAppBar(
                        selectedCount = selectedProductIds.size,
                        onClose = viewModel::exitMultiSelectMode,
                        onDelete = {
                            viewModel.deleteSelectedProducts()
                            Toast.makeText(context, "Выбранные товары удалены.", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    // ОБЫЧНЫЙ РЕЖИМ
                    DefaultTopAppBar(
                        productsSize = products.size,
                        onImportClick = {
                            filePickerLauncher.launch(arrayOf("text/*", "application/csv", "application/vnd.ms-excel"))
                        }
                    )
                }

                // Поле для поиска (скрываем в режиме выбора)
                if (!isMultiSelectMode) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        label = { Text("Поиск по названию или категории...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Поиск") },

                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    viewModel.updateSearchQuery("")
                                    focusManager.clearFocus()
                                }) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Очистить поиск"
                                    )
                                }
                            }
                        },

                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.outline,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),

                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        },
        floatingActionButton = {
            // Кнопка '+' видна только в обычном режиме
            if (!isMultiSelectMode) {
                FloatingActionButton(onClick = {
                    navController.navigate("product_edit/0")
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Добавить товар")
                }
            }
        }
    ) { paddingValues ->

        // 4. ОТОБРАЖЕНИЕ СПИСКА ТОВАРОВ (с учетом поиска)
        if (products.isEmpty() && searchQuery.isBlank()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Прайс-лист пуст. Импортируйте данные!")
            }
        } else if (products.isEmpty() && searchQuery.isNotBlank()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Ничего не найдено по запросу \"$searchQuery\"")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                items(products, key = { it.id }) { product ->
                    val isSelected = selectedProductIds.contains(product.id)

                    ProductItem(
                        product = product,
                        isSelected = isSelected, // Передаем состояние выбора
                        onClick = {
                            if (isMultiSelectMode) {
                                viewModel.toggleProductSelection(product.id) // Переключить выбор в режиме выбора
                            } else {
                                navController.navigate("product_edit/${product.id}") // Навигация в обычном режиме
                            }
                        },
                        onLongClick = {
                            viewModel.toggleProductSelection(product.id) // Долгий тап всегда переключает выбор
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

// =================================================================================
// 5. НОВЫЕ КОМПОНЕНТЫ TOP APP BAR
// =================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultTopAppBar(productsSize: Int, onImportClick: () -> Unit) {
    TopAppBar(
        title = { Text("Товары и Прайс-лист ($productsSize)") },
        actions = {
            IconButton(onClick = onImportClick) {
                Icon(Icons.Filled.FileUpload, contentDescription = "Импорт прайс-листа")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSelectTopAppBar(selectedCount: Int, onClose: () -> Unit, onDelete: () -> Unit) {
    TopAppBar(
        title = { Text(if (selectedCount == 0) "Выберите элементы" else "$selectedCount выбрано") },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Закрыть режим выбора")
            }
        },
        actions = {
            if (selectedCount > 0) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Удалить выбранные")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant // Визуально выделяем режим
        )
    )
}

// =================================================================================
// 6. ОБНОВЛЕНИЕ КОМПОНЕНТА ОТОБРАЖЕНИЯ ОДНОГО ТОВАРА
// =================================================================================

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductItem(
    product: Product,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    // Определяем цвет фона в зависимости от выбора
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent // Используем прозрачный фон в обычном состоянии
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable( // Используем combinedClickable
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress) // Тактильный отклик
                    onLongClick()
                }
            )
            .background(backgroundColor) // Применяем фон
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val categoryText = product.category.let { if (it.isNullOrBlank()) "Нет" else it }
            Text(product.name, style = MaterialTheme.typography.titleMedium)
            Text("Категория: $categoryText", style = MaterialTheme.typography.bodySmall)
        }
        Text(
            // Используем форматирование для цены, добавляя " руб / "
            // Цена (2 знака после запятой) + " руб / " + Единица измерения
            String.format(java.util.Locale.getDefault(), "%.2f руб / %s", product.unitPrice, product.unit),
            style = MaterialTheme.typography.titleMedium
        )

        // НОВОЕ: Иконка галочки в режиме выбора
        if (isSelected) {
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.Filled.Check,
                contentDescription = "Выбрано",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}