package com.example.salesanalysisapp.utils

import com.example.salesanalysisapp.data.models.Product
import java.io.InputStream

object PriceListImporter {

    // Разделитель, который используется в вашем файле (например, точка с запятой)
    private const val SEPARATOR = ";"

    fun importProducts(inputStream: InputStream): List<Product> {
        val products = mutableListOf<Product>()

        // Читаем поток данных (файл) построчно
        inputStream.bufferedReader().useLines { lines ->
            // Пропускаем первую строку (заголовок/шапку файла)
            lines.drop(1).forEach { line ->
                // Разделяем строку на части по разделителю
                val parts = line.split(SEPARATOR)

                // Проверяем, что в строке достаточно данных (минимум 4 поля: имя, цена, ед.изм., категория)
                if (parts.size >= 4) {
                    try {
                        val name = parts[0].trim()
                        // Пытаемся преобразовать строку в число Double
                        val unitPrice = parts[1].trim().replace(',', '.').toDouble()
                        val unit = parts[2].trim()
                        val category = parts[3].trim()
                        val sku = parts.getOrNull(4)?.trim() // Артикул - опционально

                        // Создаем объект Product
                        val product = Product(
                            name = name,
                            unitPrice = unitPrice,
                            unit = unit,
                            category = category,
                            sku = sku
                        )
                        products.add(product)
                    } catch (e: NumberFormatException) {
                        // Обработка ошибок, если цена указана неверно
                        println("Ошибка парсинга цены в строке: $line. Ошибка: ${e.message}")
                    }
                }
            }
        }
        return products
    }
}