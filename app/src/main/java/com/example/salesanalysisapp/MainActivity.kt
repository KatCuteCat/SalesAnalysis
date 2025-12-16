package com.example.salesanalysisapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.salesanalysisapp.ui.screens.MainAppScreen // <--- Изменили импорт
import com.example.salesanalysisapp.ui.theme.SalesAnalysisAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SalesAnalysisAppTheme {
                // Запускаем главный экран-контейнер
                MainAppScreen()
            }
        }
    }
}