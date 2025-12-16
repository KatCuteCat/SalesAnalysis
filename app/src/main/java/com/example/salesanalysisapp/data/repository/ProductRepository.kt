package com.example.salesanalysisapp.data.repository

import com.example.salesanalysisapp.data.dao.ProductDao
import com.example.salesanalysisapp.data.models.Product
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {


    fun searchProducts(searchQuery: String): Flow<List<Product>> {
        return productDao.getProductsBySearch(searchQuery)
    }

    // 2. Вставка/обновление товара
    suspend fun insert(product: Product) {
        productDao.insert(product)
    }


    suspend fun bulkInsert(products: List<Product>) {
        // productDao.deleteAll() <-- ЭТУ СТРОКУ МЫ УДАЛИЛИ

        products.forEach { importedProduct ->
            // 1. Попытка найти существующий ID по имени
            val existingId = productDao.getProductIdByName(importedProduct.name)

            val productToSave = if (existingId != null) {

                importedProduct.copy(id = existingId)
            } else {
                // 3. Если товар не найден, вставляем его как новый (id=0)
                importedProduct
            }

            productDao.insert(productToSave)
        }
    }

    // 4. Удаление товара
    suspend fun delete(product: Product) {
        productDao.delete(product)
    }

    // 5. Получение товара по ID
    suspend fun getProductById(productId: Int): Product? {
        return productDao.getProductById(productId)
    }

    suspend fun deleteAllProducts() {
        productDao.deleteAll()
    }

    suspend fun deleteProductsByIds(productIds: List<Int>) {
        productDao.deleteProductsByIds(productIds)
    }
}