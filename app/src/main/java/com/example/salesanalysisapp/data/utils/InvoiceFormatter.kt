package com.example.salesanalysisapp.data.utils

import com.example.salesanalysisapp.data.relations.OrderWithItemsAndCustomer
import com.example.salesanalysisapp.ui.screens.order.OrderItemDisplay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object InvoiceFormatter {

    // Обновляем сигнатуру, чтобы принимать List<OrderItemDisplay>
    fun formatInvoice(
        orderData: OrderWithItemsAndCustomer,
        displayedItems: List<OrderItemDisplay>
    ): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val order = orderData.order
        val customer = orderData.customer
        val totalAmount = displayedItems.sumOf { it.item.quantity * it.item.priceAtSale }

        val builder = StringBuilder()

        // === ЗАГОЛОВОК ===
        builder.append("=========================================\n")
        builder.append("             ТОВАРНЫЙ ЧЕК №${order.id}\n")
        builder.append("=========================================\n")
        builder.append("Дата оформления: ${dateFormat.format(Date(order.date))}\n")
        builder.append("Клиент: ${customer.name}\n")
        builder.append("Статус: ${order.status}\n")
        builder.append("-----------------------------------------\n")

        // === ТАБЛИЦА ТОВАРОВ ===
        builder.append(String.format(Locale.getDefault(), "%-20s %8s %5s %8s\n", "Товар", "Цена", "Кол", "Сумма"))
        builder.append("-----------------------------------------\n")

        displayedItems.forEach { displayItem ->
            val item = displayItem.item
            val name = if (displayItem.displayName.length > 20) {
                // Обрезка длинного имени для ровного столбца
                displayItem.displayName.substring(0, 17) + "..."
            } else {
                displayItem.displayName
            }

            builder.append(
                String.format(
                    Locale.getDefault(),
                    "%-20s %8.2f %5d %8.2f\n",
                    name,
                    item.priceAtSale,
                    item.quantity,
                    item.quantity * item.priceAtSale
                )
            )
        }
        builder.append("-----------------------------------------\n")

        // === ИТОГ ===
        builder.append(String.format(Locale.getDefault(), "%-33s %8.2f\n", "ИТОГО:", totalAmount))
        builder.append("=========================================\n")

        return builder.toString()
    }
}