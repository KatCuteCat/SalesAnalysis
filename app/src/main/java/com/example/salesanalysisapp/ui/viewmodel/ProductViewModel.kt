package com.example.salesanalysisapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.salesanalysisapp.data.AppDatabase
import com.example.salesanalysisapp.data.repository.ProductRepository
import com.example.salesanalysisapp.data.models.Product
import com.example.salesanalysisapp.utils.PriceListImporter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.* // <--- ВАЖНЫЙ ИМПОРТ: Flow, StateFlow, debounce, flatMapLatest
import kotlinx.coroutines.launch
import java.io.InputStream

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val productRepository: ProductRepository

    // 1. StateFlow для хранения поискового запроса (по умолчанию пустой)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 2. allProducts теперь реактивно зависит от searchQuery
    val allProducts: Flow<List<Product>>

    init {
        val productDao = AppDatabase.getDatabase(application).productDao()
        productRepository = ProductRepository(productDao)

        // Инициализация allProducts:
        // 1. Слушаем _searchQuery
        // 2. Ждем 300мс (debounce), чтобы избежать слишком частых запросов
        // 3. Выполняем поиск в БД (flatMapLatest)
        allProducts = _searchQuery
            .debounce(300)
            .flatMapLatest { query ->
                // Используем новую функцию поиска из репозитория (searchProducts)
                productRepository.searchProducts(query)
            }
            // Делаем Flow горячим, чтобы он оставался активным, пока UI его слушает
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)
    }


    fun updateSearchQuery(newQuery: String) {
        _searchQuery.value = newQuery
    }

    /**
     * Запускает асинхронный импорт прайс-листа из InputStream (файла)
     */
    fun importPriceList(inputStream: InputStream) {
        viewModelScope.launch {
            val productsToInsert = PriceListImporter.importProducts(inputStream)
            if (productsToInsert.isNotEmpty()) {
                productRepository.bulkInsert(productsToInsert)
                println("Импорт завершен. Вставлено ${productsToInsert.size} товаров.")
            }
        }
    }

    // Получение товара по ID
    suspend fun getProductById(productId: Int): Product? {
        return productRepository.getProductById(productId)
    }

    // СОЗДАНИЕ/ОБНОВЛЕНИЕ: Вставляет или обновляет товар.
    fun saveProduct(product: Product) = viewModelScope.launch {
        productRepository.insert(product)
    }

    // Функция для удаления товара
    fun deleteProduct(product: Product) = viewModelScope.launch {
        productRepository.delete(product)
    }

    // 1. Набор ID выбранных товаров
    private val _selectedProductIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedProductIds: StateFlow<Set<Int>> = _selectedProductIds.asStateFlow()

    // 2. Флаг, указывающий, активен ли режим множественного выбора
    val isMultiSelectMode: StateFlow<Boolean> = _selectedProductIds
        .map { it.isNotEmpty() } // Режим активен, если что-то выбрано
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    // --- ФУНКЦИИ УПРАВЛЕНИЯ ---

    /**
     * Добавляет/удаляет товар из набора выбранных.
     */
    fun toggleProductSelection(productId: Int) {
        _selectedProductIds.update { currentSelection ->
            if (currentSelection.contains(productId)) {
                currentSelection - productId // Удалить
            } else {
                currentSelection + productId // Добавить
            }
        }
    }

    /**
     * Очищает набор выбранных товаров и выходит из режима выбора.
     */
    fun exitMultiSelectMode() {
        _selectedProductIds.value = emptySet()
    }

    /**
     * Выполняет пакетное удаление всех выбранных товаров.
     */
    fun deleteSelectedProducts() = viewModelScope.launch {
        val idsToDelete = _selectedProductIds.value.toList()
        if (idsToDelete.isNotEmpty()) {
            productRepository.deleteProductsByIds(idsToDelete)
            // После удаления очищаем режим выбора
            exitMultiSelectMode()
        }
    }
}