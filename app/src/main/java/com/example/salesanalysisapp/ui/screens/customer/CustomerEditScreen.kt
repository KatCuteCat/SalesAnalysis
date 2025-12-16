package com.example.salesanalysisapp.ui.screens.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.salesanalysisapp.data.models.Customer
import com.example.salesanalysisapp.ui.viewmodel.CustomerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerEditScreen(
    navController: NavController,
    customerId: Int,
    viewModel: CustomerViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    // Состояния для хранения данных формы
    var name by remember { mutableStateOf("") }
    var contactPerson by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    // Флаг, является ли это редактированием или созданием
    val isEditing = customerId != 0

    // Загрузка данных клиента, если это редактирование
    LaunchedEffect(customerId) {
        if (isEditing) {
            viewModel.getCustomerById(customerId)?.let { customer ->
                name = customer.name
                contactPerson = customer.contactPerson ?: ""
                phone = customer.phone ?: ""
                address = customer.address ?: ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Редактировать клиента" else "Новый клиент") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (isEditing) {
                        // Кнопка удаления
                        TextButton(onClick = {
                            coroutineScope.launch {
                                // Загружаем объект клиента для удаления
                                viewModel.getCustomerById(customerId)?.let { customer ->
                                    viewModel.deleteCustomer(customer)
                                    navController.popBackStack() // Возвращаемся после удаления
                                }
                            }
                        }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                            Text("Удалить")
                        }
                    }
                }
            )
        },
        bottomBar = {
            // Кнопка Сохранить
            Button(
                onClick = {
                    val customerToSave = Customer(
                        id = if (isEditing) customerId else 0,
                        name = name,
                        contactPerson = contactPerson.ifEmpty { null },
                        phone = phone.ifEmpty { null },
                        address = address.ifEmpty { null }
                    )
                    viewModel.insertCustomer(customerToSave)
                    navController.popBackStack() // Возвращаемся к списку
                },
                // Кнопка неактивна, если не заполнено обязательное поле "Имя"
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(if (isEditing) "Сохранить изменения" else "Создать клиента")
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Поле 1: Название/ФИО (Обязательное)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название компании / ФИО *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Поле 2: Контактное лицо
            OutlinedTextField(
                value = contactPerson,
                onValueChange = { contactPerson = it },
                label = { Text("Контактное лицо") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Поле 3: Телефон
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Телефон") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Поле 4: Адрес
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Адрес") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}