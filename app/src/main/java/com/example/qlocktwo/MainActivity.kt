package com.example.qlocktwo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.qlocktwo.screens.MainScreen
import com.example.qlocktwo.ui.theme.QlocktwoTheme
import com.example.qlocktwo.viewmodels.ColorViewModel
import com.example.qlocktwo.viewmodels.ColorViewModelFactory
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.first

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

            var isLoading by remember { mutableStateOf(true) }

            // Warte auf initiale Nachricht oder Timeout
            LaunchedEffect(webSocketManager) {
                // Warte maximal 5 Sekunden auf eine SETTINGS- oder TEMP-Nachricht
                val result = withTimeoutOrNull(5000) {
                    webSocketManager.messages.first { msg ->
                        msg.startsWith("SETTINGS:") || msg.startsWith("TEMP:")
                    }
                }

                if (result != null) {
                    println("Initial message received: $result")
                } else {
                    println("Loading timeout - showing UI anyway")
                }
                isLoading = false
            }

            // Kontinuierlich auf SETTINGS-Nachrichten hören und ColorViewModel aktualisieren
            LaunchedEffect(webSocketManager) {
                webSocketManager.currentSettings.collect { settingsMsg ->
                    settingsMsg?.let { msg ->
                        println("MainActivity processing SETTINGS: $msg")
                        if (msg.startsWith("SETTINGS:")) {
                            val parts = msg.removePrefix("SETTINGS:").split(",").map { it.trim() }
                            if (parts.size >= 5) {
                                val r = parts[1].toIntOrNull() ?: 255
                                val g = parts[2].toIntOrNull() ?: 0
                                val b = parts[3].toIntOrNull() ?: 0
                                val brightness = parts[4].toFloatOrNull() ?: 255f
                                colorViewModel.updateColor(androidx.compose.ui.graphics.Color(r / 255f, g / 255f, b / 255f))
                                colorViewModel.updateBrightness(brightness)
                                println("Settings applied to ColorViewModel: r=$r g=$g b=$b brightness=$brightness")
                            }
                        }
                    }
                }
            }

            QlocktwoTheme(darkTheme = true) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Text("Verbinde mit Uhr...", modifier = Modifier.padding(top = 16.dp))
                        }
                    }
                } else {
                    MainScreen(
                        webSocketManager = webSocketManager,
                        colorViewModel = colorViewModel
                    )
                }
            }
        }
    }
}
