package com.example.salesanalysisapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.salesanalysisapp.data.AppDatabase
import com.example.salesanalysisapp.data.analytics.SalesByMonth
import com.example.salesanalysisapp.data.models.Customer
import com.example.salesanalysisapp.data.models.Order
import com.example.salesanalysisapp.data.models.OrderItem
import com.example.salesanalysisapp.data.models.Product
import com.example.salesanalysisapp.data.relations.OrderWithCustomer
import com.example.salesanalysisapp.data.repository.CustomerRepository
import com.example.salesanalysisapp.data.repository.OrderRepository
import com.example.salesanalysisapp.data.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import com.example.salesanalysisapp.data.analytics.ABCItem
import com.example.salesanalysisapp.data.analytics.ProductSales
import com.example.salesanalysisapp.data.analytics.XYZItem
import com.example.salesanalysisapp.data.analytics.ProductMonthlySale
import com.example.salesanalysisapp.data.analytics.AggregateSalesMetrics
import com.example.salesanalysisapp.data.analytics.TopProduct
import com.example.salesanalysisapp.data.relations.OrderWithItemsAndCustomer
import com.example.salesanalysisapp.data.utils.TestDataGenerator

class OrderViewModel(application: Application) : AndroidViewModel(application) {


    private val repository: OrderRepository
    private val productRepository: ProductRepository
    private val customerRepository: CustomerRepository




    val allOrdersWithCustomer: Flow<List<OrderWithCustomer>>
    val allProducts: Flow<List<Product>>
    val allCustomers: Flow<List<Customer>>

    init {
        val database = AppDatabase.getDatabase(application)

        // Инициализация Order
        val orderDao = database.orderDao()
        repository = OrderRepository(orderDao)
        allOrdersWithCustomer = repository.allOrdersWithCustomer

        // Инициализация Product
        productRepository = ProductRepository(database.productDao())
        // ИСПРАВЛЕНИЕ: Вызываем новую функцию поиска с пустым запросом, чтобы получить все товары.
        allProducts = productRepository.searchProducts("") // <-- ИСПРАВЛЕННАЯ СТРОКА

        // Инициализация Customer
        customerRepository = CustomerRepository(database.customerDao())
        allCustomers = customerRepository.allCustomers
    }

    // =======================================================
    // ФУНКЦИИ ДЛЯ РАБОТЫ С ЗАКАЗАМИ
    // =======================================================

    fun createOrder(order: Order, items: List<OrderItem>) = viewModelScope.launch {
        repository.createOrder(order, items)
    }

    fun updateOrderStatus(orderId: Int, newStatus: String) = viewModelScope.launch {
        repository.updateOrderStatus(orderId, newStatus)
    }

    // ... (getOrderItems, getOrderById) ...


    suspend fun getSalesDynamics(): List<SalesByMonth> {
        return repository.getSalesDynamics() // Теперь repository доступен!
    }


    suspend fun getProductById(id: Int): Product? {
        return productRepository.getProductById(id)
    }

    suspend fun getCustomerById(id: Int): Customer? {
        return customerRepository.getCustomerById(id)
    }


    /**
     * Выполняет ABC-анализ товаров (по доле в выручке).
     * Группы: A (>80%), B (80%-95%), C (<95%).
     */
    suspend fun getABCAnalysis(): List<ABCItem> {
        val salesData = repository.getProductSalesForABC()
        if (salesData.isEmpty()) return emptyList()

        val totalRevenue = salesData.sumOf { it.totalRevenue }
        if (totalRevenue == 0.0) return emptyList()

        var cumulativePercentage = 0.0
        val abcResults = mutableListOf<ABCItem>()

        // Сортировка по убыванию выручки (уже сделана в SQL, но Kotlin-код более надежен)
        val sortedSalesData = salesData.sortedByDescending { it.totalRevenue }

        sortedSalesData.forEach { productSales ->
            val percentage = (productSales.totalRevenue / totalRevenue) * 100
            cumulativePercentage += percentage

            // Определение группы ABC по общепринятым стандартам (80/15/5)
            val abcGroup = when {
                cumulativePercentage <= 80.0 -> "A" // Важнейшие товары, >80% дохода
                cumulativePercentage <= 95.0 -> "B" // Промежуточные товары, 80%-95% дохода
                else -> "C"                         // Менее важные, <5% дохода
            }

            abcResults.add(
                ABCItem(
                    productId = productSales.productId,
                    productName = productSales.productName,
                    totalRevenue = productSales.totalRevenue,
                    percentage = percentage,
                    cumulativePercentage = cumulativePercentage,
                    abcGroup = abcGroup
                )
            )
        }

        return abcResults
    }

    /**
     * Выполняет XYZ-анализ товаров (по стабильности спроса).
     * Группы: X (стабильный спрос, CV < 10%), Y (колеблющийся, 10% <= CV <= 25%), Z (нерегулярный, CV > 25%).
     */
    suspend fun getXYZAnalysis(): List<XYZItem> {

        val monthlySalesData = repository.getProductMonthlySales()
        if (monthlySalesData.isEmpty()) return emptyList()

        // Группировка данных по товару
        val salesByProduct = monthlySalesData.groupBy { it.productId }

        val xyzResults = mutableListOf<XYZItem>()


        salesByProduct.forEach { (productId, monthlySales) ->

            // Извлечение только проданных количеств за каждый период
            val quantities = monthlySales.map { it.quantitySold.toDouble() }


            if (quantities.size < 3) return@forEach

            val avgMonthlySales = quantities.average()

            // Расчет стандартного отклонения (Standard Deviation, σ)
            // ИСПРАВЛЕНИЕ: Расчет с использованием quantities
            val variance = quantities.sumOf { (it - avgMonthlySales) * (it - avgMonthlySales) } / quantities.size
            val standardDeviation = kotlin.math.sqrt(variance)

            // Расчет Коэффициента Вариации (Coefficient of Variation, CV)
            val coefficientOfVariation = if (avgMonthlySales > 0) {
                (standardDeviation / avgMonthlySales) * 100
            } else {
                100.0 // Если среднее равно 0, вариация максимальная
            }

            // Определение группы XYZ
            val xyzGroup = when {
                coefficientOfVariation < 10.0 -> "X"
                coefficientOfVariation <= 25.0 -> "Y"
                else -> "Z"
            }

            val productName = monthlySales.first().let {
                // ИСПРАВЛЕНИЕ: Вызываем getProductById с productId
                productRepository.getProductById(productId)?.name ?: "Неизвестный товар"
            }

            xyzResults.add(
                XYZItem(
                    productId = productId,
                    productName = productName,
                    avgMonthlySales = avgMonthlySales,
                    coefficientOfVariation = coefficientOfVariation,
                    xyzGroup = xyzGroup
                )
            )
        }

        return xyzResults.sortedBy { it.coefficientOfVariation }
    }

    /**
     * Получает общую выручку и количество заказов.
     */
    suspend fun getAggregateSalesMetrics(): AggregateSalesMetrics? {
        return repository.getAggregateSalesMetrics()
    }

    /**
     * Получает ТОП-5 самых продаваемых товаров.
     */
    suspend fun getTopSellingProducts(): List<TopProduct> {
        return repository.getTopSellingProducts()
    }

    suspend fun getFullOrderById(orderId: Int): OrderWithItemsAndCustomer? {
        return repository.getFullOrderById(orderId)
    }

    /**
     * Генерирует и вставляет полный набор тестовых данных.
     */
    suspend fun generateTestData() {
        // 1. Очистка базы данных
        repository.deleteAllOrders()
        customerRepository.deleteAllCustomers()
        productRepository.deleteAllProducts()

        // 2. Вставка тестовых данных

        TestDataGenerator.TEST_PRODUCTS.forEach { productRepository.insert(it) }
        TestDataGenerator.TEST_CUSTOMERS.forEach { customerRepository.insert(it) }

        // Вставка заказов и деталей
        TestDataGenerator.generateOrders().forEach { (order, items) ->
            repository.createOrder(order, items)
        }
    }
}