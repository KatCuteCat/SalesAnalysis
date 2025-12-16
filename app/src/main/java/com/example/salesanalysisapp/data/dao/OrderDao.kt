package com.example.salesanalysisapp.data.dao

import androidx.room.*
import com.example.salesanalysisapp.data.analytics.AggregateSalesMetrics
import com.example.salesanalysisapp.data.models.Order
import com.example.salesanalysisapp.data.models.OrderItem
import com.example.salesanalysisapp.data.relations.OrderWithCustomer
import kotlinx.coroutines.flow.Flow
import com.example.salesanalysisapp.data.analytics.SalesByMonth
import com.example.salesanalysisapp.data.analytics.ProductSales
import com.example.salesanalysisapp.data.analytics.ProductMonthlySale
import com.example.salesanalysisapp.data.analytics.TopProduct
import com.example.salesanalysisapp.data.relations.OrderWithItemsAndCustomer

@Dao
interface OrderDao {

    // === Операции с Order (Заголовки) ===

    // ЧТЕНИЕ: Получить все заказы, объединенные с клиентом
    @Transaction
    @Query("SELECT * FROM orders ORDER BY date DESC")
    fun getAllOrdersWithCustomer(): Flow<List<OrderWithCustomer>>

    // ЧТЕНИЕ: Получить заказ по ID (ИСПРАВЛЕНО/ДОБАВЛЕНО)
    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: Int): Order?

    // СОЗДАНИЕ/ОБНОВЛЕНИЕ: Вставить или обновить заказ
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long // <-- БЫЛО ССЫЛКА В РЕПОЗИТОРИИ

    // ОБНОВЛЕНИЕ: Обновить только статус
    @Query("UPDATE orders SET status = :newStatus WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Int, newStatus: String) // <-- БЫЛО ССЫЛКА В РЕПОЗИТОРИИ

    // === Операции с OrderItem (Детализация) ===

    // СОЗДАНИЕ: Вставить одну деталь заказа
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItem(orderItem: OrderItem)

    // СОЗДАНИЕ: Вставить несколько деталей заказа (при создании заказа)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItem>) // <-- БЫЛО ССЫЛКА В РЕПОЗИТОРИИ

    // ЧТЕНИЕ: Получить все детали по конкретному заказу
    @Query("""
        SELECT
            oi.*, p.unitPrice, p.name AS productName, p.unit
        FROM order_items oi
        JOIN products p ON oi.productId = p.id
        WHERE oi.orderId = :orderId
    """)
    suspend fun getItemsForOrder(orderId: Int): List<OrderItem>

    /**
     * ЧТЕНИЕ: Получить полную структуру заказа по ID (включая клиента и детали).
     */
    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getFullOrderById(orderId: Int): OrderWithItemsAndCustomer?

    @Query("""
        SELECT 
            strftime('%Y-%m', datetime(o.date / 1000, 'unixepoch')) AS yearMonth,
            SUM(oi.quantity * oi.priceAtSale) AS totalSales
        FROM orders o
        JOIN order_items oi ON o.id = oi.orderId
        GROUP BY yearMonth
        ORDER BY yearMonth ASC
    """)
    suspend fun getSalesDynamics(): List<SalesByMonth>


    // === Операции для Аналитики (добавляем новую функцию) ===

    /**
     * Рассчитывает общую выручку по каждому товару.
     */
    @Query("""
        SELECT 
            oi.productId, 
            p.name AS productName,
            SUM(oi.quantity * oi.priceAtSale) AS totalRevenue
        FROM order_items oi
        JOIN products p ON oi.productId = p.id
        GROUP BY oi.productId, p.name
        ORDER BY totalRevenue DESC
    """)
    suspend fun getProductSalesForABC(): List<ProductSales>

    /**
     * Собирает проданное количество каждого товара по месяцам.
     * Необходим для расчета коэффициента вариации (XYZ-анализ).
     */
    @Query("""
        SELECT 
            oi.productId, 
            strftime('%Y-%m', datetime(o.date / 1000, 'unixepoch')) AS yearMonth,
            SUM(oi.quantity) AS quantitySold
        FROM orders o
        JOIN order_items oi ON o.id = oi.orderId
        GROUP BY oi.productId, yearMonth
        HAVING quantitySold > 0
        ORDER BY oi.productId ASC, yearMonth ASC
    """)
    suspend fun getProductMonthlySales(): List<ProductMonthlySale>

    /**
     * Рассчитывает общую сумму всех продаж и количество заказов.
     * Необходим для расчета среднего чека.
     */
    @Query("""
        SELECT 
            SUM(oi.quantity * oi.priceAtSale) AS totalRevenue,
            COUNT(DISTINCT o.id) AS totalOrders
        FROM orders o
        JOIN order_items oi ON o.id = oi.orderId
    """)
    suspend fun getAggregateSalesMetrics(): AggregateSalesMetrics?

    /**
     * Возвращает ТОП-5 товаров по проданному количеству.
     */
    @Query("""
        SELECT 
            p.name AS productName,
            SUM(oi.quantity) AS quantitySold
        FROM order_items oi
        JOIN products p ON oi.productId = p.id
        GROUP BY p.id, p.name
        ORDER BY quantitySold DESC
        LIMIT 5
    """)
    suspend fun getTopSellingProducts(): List<TopProduct>


    @Query("DELETE FROM order_items")
    suspend fun deleteAllOrderItems()

    @Query("DELETE FROM orders")
    suspend fun deleteAllOrders()
}