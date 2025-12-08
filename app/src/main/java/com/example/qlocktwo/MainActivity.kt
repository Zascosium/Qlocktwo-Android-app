package com.example.qlocktwo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qlocktwo.screens.MainScreen
import com.example.qlocktwo.ui.theme.QlocktwoTheme
import com.example.qlocktwo.viewmodels.ColorViewModel
import com.example.qlocktwo.viewmodels.ColorViewModelFactory

class MainActivity : ComponentActivity() {

    private val webSocketManager = WebSocketManager()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        webSocketManager.connectWithPrefs(this)
        setContent {
            val colorViewModel: ColorViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = ColorViewModelFactory(application)
            )
            QlocktwoTheme(darkTheme = true) {
                MainScreen(
                    webSocketManager = webSocketManager,
                    colorViewModel = colorViewModel
                )
            }
        }
    }
}
