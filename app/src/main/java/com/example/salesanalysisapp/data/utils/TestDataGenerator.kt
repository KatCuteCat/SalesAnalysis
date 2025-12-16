package com.example.salesanalysisapp.data.utils

import com.example.salesanalysisapp.data.models.Customer
import com.example.salesanalysisapp.data.models.Order
import com.example.salesanalysisapp.data.models.OrderItem
import com.example.salesanalysisapp.data.models.Product
import java.util.*

object TestDataGenerator {

    // Тестовые клиенты с заполненными всеми полями
    val TEST_CUSTOMERS = listOf(
        Customer(id = 1, name = "ООО 'Альфа'", contactPerson = "Иванов И.И.", phone = "8-800-111", address = "г. Москва"),
        Customer(id = 2, name = "ИП Петров", contactPerson = "Петров П.П.", phone = "8-800-222", address = "г. Санкт-Петербург"),
        Customer(id = 3, name = "ЗАО 'Гамма'", contactPerson = "Сидоров С.С.", phone = "8-800-333", address = "г. Казань")
    )

    // Тестовые товары с заполненными категориями
    val TEST_PRODUCTS = listOf(
        // Индекс 0
        Product(id = 1, name = "Премиум-кофе 'Arabica'", unitPrice = 1200.0, unit = "кг", category = "Напитки"),
        // Индекс 1
        Product(id = 2, name = "Молоко 'Фермерское'", unitPrice = 150.0, unit = "л", category = "Молочное"),
        // Индекс 2
        Product(id = 3, name = "Сахар-песок", unitPrice = 65.0, unit = "кг", category = "Бакалея"),
        // Индекс 3
        Product(id = 4, name = "Особый чай 'Пуэр'", unitPrice = 900.0, unit = "шт", category = "Напитки"),
        // Индекс 4
        Product(id = 5, name = "Печенье 'Юбилейное'", unitPrice = 80.0, unit = "уп.", category = "Бакалея")
    )

    /**
     * Генерирует 5 заказов, распределенных по 5 месяцам.
     */
    fun generateOrders(): List<Pair<Order, List<OrderItem>>> {
        val orders = mutableListOf<Pair<Order, List<OrderItem>>>()

        // Откатываемся на 4 месяца назад
        val calendar = Calendar.getInstance().apply {
            add(Calendar.MONTH, -4)
        }

        val dates = listOf(
            calendar.timeInMillis,
            calendar.apply { add(Calendar.MONTH, 1); add(Calendar.DAY_OF_YEAR, 5) }.timeInMillis,
            calendar.apply { add(Calendar.MONTH, 1); add(Calendar.DAY_OF_YEAR, 10) }.timeInMillis,
            calendar.apply { add(Calendar.MONTH, 1); add(Calendar.DAY_OF_YEAR, 15) }.timeInMillis,
            calendar.apply { add(Calendar.MONTH, 1); add(Calendar.DAY_OF_YEAR, 20) }.timeInMillis
        )

        // === ИНДЕКСЫ: Товар 1 -> 0, Товар 2 -> 1, Товар 3 -> 2, Товар 4 -> 3, Товар 5 -> 4 ===

        // Заказ 1: Месяц 1. A/X, B/Y
        orders.add(createOrder(1, dates[0], 1, listOf(
            // ИСПРАВЛЕНИЕ 1: Товар 1
            OrderItem(orderId = 0, productId = 1, quantity = 10, priceAtSale = TEST_PRODUCTS[0].unitPrice, productNameAtSale = TEST_PRODUCTS[0].name),
            // ИСПРАВЛЕНИЕ 2: Товар 2
            OrderItem(orderId = 0, productId = 2, quantity = 25, priceAtSale = TEST_PRODUCTS[1].unitPrice, productNameAtSale = TEST_PRODUCTS[1].name)
        )))

        // Заказ 2: Месяц 2. A/X, C/C, B/Y
        orders.add(createOrder(2, dates[1], 2, listOf(
            // ИСПРАВЛЕНИЕ 3: Товар 1
            OrderItem(orderId = 0, productId = 1, quantity = 12, priceAtSale = TEST_PRODUCTS[0].unitPrice, productNameAtSale = TEST_PRODUCTS[0].name),
            // ИСПРАВЛЕНИЕ 4: Товар 3
            OrderItem(orderId = 0, productId = 3, quantity = 50, priceAtSale = TEST_PRODUCTS[2].unitPrice, productNameAtSale = TEST_PRODUCTS[2].name),
            // ИСПРАВЛЕНИЕ 5: Товар 5
            OrderItem(orderId = 0, productId = 5, quantity = 40, priceAtSale = TEST_PRODUCTS[4].unitPrice, productNameAtSale = TEST_PRODUCTS[4].name)
        )))

        // Заказ 3: Месяц 3. A/X, B/Y
        orders.add(createOrder(3, dates[2], 1, listOf(
            // ИСПРАВЛЕНИЕ 6: Товар 1
            OrderItem(orderId = 0, productId = 1, quantity = 11, priceAtSale = TEST_PRODUCTS[0].unitPrice, productNameAtSale = TEST_PRODUCTS[0].name),
            // ИСПРАВЛЕНИЕ 7: Товар 2
            OrderItem(orderId = 0, productId = 2, quantity = 30, priceAtSale = TEST_PRODUCTS[1].unitPrice, productNameAtSale = TEST_PRODUCTS[1].name)
        )))

        // Заказ 4: Месяц 4. D/Z
        orders.add(createOrder(4, dates[3], 3, listOf(
            // ИСПРАВЛЕНИЕ 8: Товар 4
            OrderItem(orderId = 0, productId = 4, quantity = 3, priceAtSale = TEST_PRODUCTS[3].unitPrice, productNameAtSale = TEST_PRODUCTS[3].name)
        )))

        // Заказ 5: Месяц 5. A/X, B/Y, C/C
        orders.add(createOrder(5, dates[4], 2, listOf(
            // ИСПРАВЛЕНИЕ 9: Товар 1
            OrderItem(orderId = 0, productId = 1, quantity = 10, priceAtSale = TEST_PRODUCTS[0].unitPrice, productNameAtSale = TEST_PRODUCTS[0].name),
            // ИСПРАВЛЕНИЕ 10: Товар 2
            OrderItem(orderId = 0, productId = 2, quantity = 28, priceAtSale = TEST_PRODUCTS[1].unitPrice, productNameAtSale = TEST_PRODUCTS[1].name),
            // ИСПРАВЛЕНИЕ 11: Товар 3
            OrderItem(orderId = 0, productId = 3, quantity = 60, priceAtSale = TEST_PRODUCTS[2].unitPrice, productNameAtSale = TEST_PRODUCTS[2].name)
        )))

        return orders
    }

    private fun createOrder(id: Int, date: Long, customerId: Int, items: List<OrderItem>): Pair<Order, List<OrderItem>> {
        val order = Order(
            id = id,
            customerId = customerId,
            date = date,
            status = "Выполнен"
        )
        return Pair(order, items)
    }
}