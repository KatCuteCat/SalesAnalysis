package com.example.salesanalysisapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.salesanalysisapp.data.dao.CustomerDao
import com.example.salesanalysisapp.data.dao.OrderDao
import com.example.salesanalysisapp.data.dao.ProductDao
import com.example.salesanalysisapp.data.models.*

@Database(
    entities = [Product::class, Customer::class, Order::class, OrderItem::class],
    version = 1, // Версия базы данных
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Объявляем DAO
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao // Добавим позже
    abstract fun orderDao(): OrderDao // Добавим позже

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sales_database"
                )

                    .build()
                INSTANCE = instance

                instance
            }
        }
    }
}