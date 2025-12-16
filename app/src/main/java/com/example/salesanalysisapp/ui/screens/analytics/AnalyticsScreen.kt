package com.example.salesanalysisapp.ui.screens.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salesanalysisapp.data.analytics.ABCItem
import com.example.salesanalysisapp.data.analytics.AggregateSalesMetrics // <--- НОВЫЙ ИМПОРТ
import com.example.salesanalysisapp.data.analytics.SalesByMonth
import com.example.salesanalysisapp.data.analytics.TopProduct // <--- НОВЫЙ ИМПОРТ
import com.example.salesanalysisapp.data.analytics.XYZItem
import com.example.salesanalysisapp.ui.viewmodel.OrderViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: OrderViewModel = viewModel()
) {
    // 1. Состояние для данных о динамике продаж
    var salesData by remember { mutableStateOf<List<SalesByMonth>>(emptyList()) }
    // 2. Состояние для данных ABC-анализа
    var abcData by remember { mutableStateOf<List<ABCItem>>(emptyList()) }
    // 3. Состояние для данных XYZ-анализа
    var xyzData by remember { mutableStateOf<List<XYZItem>>(emptyList()) }
    // 4. Состояние для агрегированных метрик
    var salesMetrics by remember { mutableStateOf<AggregateSalesMetrics?>(null) }
    // 5. Состояние для ТОП-товаров
    var topProducts by remember { mutableStateOf<List<TopProduct>>(emptyList()) }

    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Функция для загрузки всех данных
    val loadData: () -> Unit = {
        isLoading = true
        coroutineScope.launch {
            salesData = viewModel.getSalesDynamics()
            abcData = viewModel.getABCAnalysis()
            xyzData = viewModel.getXYZAnalysis()
            salesMetrics = viewModel.getAggregateSalesMetrics()
            topProducts = viewModel.getTopSellingProducts()
            isLoading = false
        }
    }

    // --- НОВАЯ ФУНКЦИЯ: ГЕНЕРАЦИЯ ДАННЫХ ---
    val generateData: () -> Unit = {
        coroutineScope.launch {
            isLoading = true
            viewModel.generateTestData()

            loadData()
            isLoading = false
            snackbarHostState.showSnackbar(
                message = "Тестовые данные успешно сгенерированы!",
                actionLabel = "OK",
                duration = SnackbarDuration.Short
            )
        }
    }

    // Загрузка данных при первом входе на экран
    LaunchedEffect(Unit) {
        loadData()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Аналитика продаж") },
                actions = {
                    // Кнопка Генерации Тестовых Данных
                    TextButton(onClick = generateData, enabled = !isLoading) {
                        Text("Тест Данные", color = MaterialTheme.colorScheme.error)
                    }
                    IconButton(onClick = loadData, enabled = !isLoading) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Обновить данные")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp) // Убрал verticalScroll изнутри, оставил только отступы
                .verticalScroll(rememberScrollState())
        ) {

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))
            }

            // === 0. КЛЮЧЕВЫЕ МЕТРИКИ (НОВАЯ СЕКЦИЯ) ===
            Text(
                "0. Ключевые показатели:",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            KeyMetricsSection(salesMetrics = salesMetrics, topProducts = topProducts)

            HorizontalDivider(Modifier.padding(vertical = 24.dp))

            // === 1. ДИНАМИКА ПРОДАЖ ===
            Text(
                "1. Динамика продаж по месяцам:",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            SalesDynamicsTable(salesData = salesData)

            HorizontalDivider(Modifier.padding(vertical = 24.dp))

            // === 2. ABC АНАЛИЗ ===
            Text(
                "2. ABC-анализ товаров (по выручке):",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            ABCAnalysisTable(abcData = abcData)

            Spacer(modifier = Modifier.height(16.dp))

            // Общее примечание
            Text(
                "ABC-анализ классифицирует товары по их доле в общем доходе: Группа A (до 80% выручки), Группа B (80%-95%), Группа C (более 95%).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(Modifier.padding(vertical = 24.dp))

            // === 3. XYZ АНАЛИЗ ===
            Text(
                "3. XYZ-анализ товаров (по стабильности):",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            XYZAnalysisTable(xyzData = xyzData)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "XYZ-анализ классифицирует товары по предсказуемости спроса: Группа X (стабильный, CV < 10%), Группа Y (колеблющийся, 10%-25%), Группа Z (нерегулярный, CV > 25%).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp)) // Небольшой отступ в конце
        }
    }
}


// =================================================================================
// Вспомогательный Composable для отображения Ключевых Метрик (ДОБАВЛЕН)
// =================================================================================

@Composable
fun KeyMetricsSection(salesMetrics: AggregateSalesMetrics?, topProducts: List<TopProduct>) {

    // Метрики (Средний чек, Общая выручка)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Карта для Среднего Чека
        Card(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Средний Чек", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = String.format(Locale.getDefault(), "%.2f ₽", salesMetrics?.averageCheck ?: 0.0),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Заказов: ${salesMetrics?.totalOrders ?: 0}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Карта для Общей Выручки
        Card(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Общая Выручка", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = String.format(Locale.getDefault(), "%.2f ₽", salesMetrics?.totalRevenue ?: 0.0),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }

    Spacer(Modifier.height(16.dp))

    // ТОП-5 Товаров
    Text(
        "ТОП-5 товаров по количеству:",
        style = MaterialTheme.typography.titleMedium
    )
    Spacer(Modifier.height(4.dp))

    if (topProducts.isEmpty()) {
        Text("Данные ТОП-товаров отсутствуют.", color = Color.Gray)
    } else {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            topProducts.forEachIndexed { index, product ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${index + 1}. ${product.productName}", style = MaterialTheme.typography.bodyMedium)
                    Text("${product.quantitySold} шт.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}


// =================================================================================
// Вспомогательные Composable для отображения таблиц (ОСТАЛЬНЫЕ)
// =================================================================================

@Composable
fun SalesDynamicsTable(salesData: List<SalesByMonth>) {
    if (salesData.isEmpty()) {
        Text("Данные о продажах отсутствуют (создайте заказы).", color = Color.Gray)
        return
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            // Заголовок таблицы
            Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Месяц", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text("Сумма (руб.)", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            }
            HorizontalDivider()
            salesData.forEach { item ->
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item.yearMonth, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text(
                        String.format(Locale.getDefault(), "%.2f", item.totalSales),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
fun ABCAnalysisTable(abcData: List<ABCItem>) {
    if (abcData.isEmpty()) {
        Text("Данные для ABC-анализа отсутствуют.", color = Color.Gray)
        return
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            // Заголовок таблицы
            Row(
                Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Гр.", style = MaterialTheme.typography.titleSmall, modifier = Modifier.width(30.dp))
                Text("Товар", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text("Выручка", style = MaterialTheme.typography.titleSmall, modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
                Text("Доля, %", style = MaterialTheme.typography.titleSmall, modifier = Modifier.width(70.dp), textAlign = TextAlign.End)
                Text("Накоп., %", style = MaterialTheme.typography.titleSmall, modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
            }
            HorizontalDivider()
            abcData.forEach { item ->
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Группа A, B, C
                    Text(
                        item.abcGroup,
                        style = MaterialTheme.typography.bodyMedium,
                        color = when(item.abcGroup) {
                            "A" -> Color(0xFF4CAF50) // Зеленый
                            "B" -> Color(0xFFFF9800) // Оранжевый
                            else -> Color(0xFFF44336) // Красный
                        },
                        modifier = Modifier.width(30.dp)
                    )
                    // Имя товара
                    Text(
                        item.productName,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    // Выручка
                    Text(
                        String.format(Locale.getDefault(), "%.2f", item.totalRevenue),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(80.dp),
                        textAlign = TextAlign.End
                    )
                    // Доля
                    Text(
                        String.format(Locale.getDefault(), "%.1f", item.percentage),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(70.dp),
                        textAlign = TextAlign.End
                    )
                    // Накопительная доля
                    Text(
                        String.format(Locale.getDefault(), "%.1f", item.cumulativePercentage),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(80.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}


@Composable
fun XYZAnalysisTable(xyzData: List<XYZItem>) {
    if (xyzData.isEmpty()) {
        Text("Данные для XYZ-анализа отсутствуют.", color = Color.Gray)
        return
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            // Заголовок таблицы
            Row(
                Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Гр.", style = MaterialTheme.typography.titleSmall, modifier = Modifier.width(30.dp))
                Text("Товар", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text("CV, %", style = MaterialTheme.typography.titleSmall, modifier = Modifier.width(60.dp), textAlign = TextAlign.End)
                Text("Средн. Объем", style = MaterialTheme.typography.titleSmall, modifier = Modifier.width(100.dp), textAlign = TextAlign.End)
            }
            HorizontalDivider()
            xyzData.forEach { item ->
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Группа X, Y, Z
                    Text(
                        item.xyzGroup,
                        style = MaterialTheme.typography.bodyMedium,
                        color = when(item.xyzGroup) {
                            "X" -> Color(0xFF4CAF50) // Зеленый
                            "Y" -> Color(0xFFFF9800) // Оранжевый
                            else -> Color(0xFFF44336) // Красный
                        },
                        modifier = Modifier.width(30.dp)
                    )
                    // Имя товара
                    Text(
                        item.productName,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    // Коэффициент вариации (CV)
                    Text(
                        String.format(Locale.getDefault(), "%.1f", item.coefficientOfVariation),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(60.dp),
                        textAlign = TextAlign.End
                    )
                    // Среднемесячный объем
                    Text(
                        String.format(Locale.getDefault(), "%.2f", item.avgMonthlySales),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(100.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}