package com.example.salesanalysisapp.data.repository

import com.example.salesanalysisapp.data.dao.OrderDao
import com.example.salesanalysisapp.data.models.Order
import com.example.salesanalysisapp.data.models.OrderItem
import com.example.salesanalysisapp.data.relations.OrderWithCustomer
import kotlinx.coroutines.flow.Flow
import com.example.salesanalysisapp.data.analytics.SalesByMonth
import com.example.salesanalysisapp.data.analytics.ProductSales
import com.example.salesanalysisapp.data.analytics.ProductMonthlySale
import com.example.salesanalysisapp.data.analytics.AggregateSalesMetrics
import com.example.salesanalysisapp.data.analytics.TopProduct
import com.example.salesanalysisapp.data.relations.OrderWithItemsAndCustomer

class OrderRepository(private val orderDao: OrderDao) {

    // 1. Получение всех заказов с клиентами
    val allOrdersWithCustomer: Flow<List<OrderWithCustomer>> = orderDao.getAllOrdersWithCustomer()

    /**
     * 2. Сохранение заказа и его деталей (Сложная транзакция)
     * Вставка Order должна произойти ПЕРЕД вставкой OrderItems.
     */

    suspend fun createOrder(order: Order, items: List<OrderItem>) {

        val orderId = orderDao.insertOrder(order)

        val itemsWithId = items.map { item ->

            item.copy(orderId = orderId.toInt())
        }

        orderDao.insertOrderItems(itemsWithId)
    }

    // 3. Обновление статуса
    suspend fun updateOrderStatus(orderId: Int, newStatus: String) {
        orderDao.updateOrderStatus(orderId, newStatus)
    }

    // 4. Получение деталей заказа
    suspend fun getOrderItems(orderId: Int): List<OrderItem> {
        return orderDao.getItemsForOrder(orderId)
    }

    // 5. Получение заголовка заказа
    suspend fun getOrderById(orderId: Int): Order? {
        return orderDao.getOrderById(orderId)
    }

    suspend fun getSalesDynamics(): List<SalesByMonth> {
        return orderDao.getSalesDynamics()
    }

    // НОВАЯ ФУНКЦИЯ: Получение выручки по товарам
    suspend fun getProductSalesForABC(): List<ProductSales> {
        return orderDao.getProductSalesForABC()
    }


    suspend fun getProductMonthlySales(): List<ProductMonthlySale> {
        return orderDao.getProductMonthlySales()
    }


    suspend fun getAggregateSalesMetrics(): AggregateSalesMetrics? {
        return orderDao.getAggregateSalesMetrics()
    }


    suspend fun getTopSellingProducts(): List<TopProduct> {
        return orderDao.getTopSellingProducts()
    }

    suspend fun getFullOrderById(orderId: Int): OrderWithItemsAndCustomer? {
        return orderDao.getFullOrderById(orderId)
    }

    suspend fun deleteAllOrders() {

        orderDao.deleteAllOrderItems()
        orderDao.deleteAllOrders()
    }
}