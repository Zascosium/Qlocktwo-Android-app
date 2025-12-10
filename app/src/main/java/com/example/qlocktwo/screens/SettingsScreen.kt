package com.example.qlocktwo.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.qlocktwo.R
import com.example.qlocktwo.WebSocketManager
import kotlinx.coroutines.flow.collectLatest
import java.util.Calendar
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qlocktwo.viewmodels.ColorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    webSocketManager: WebSocketManager,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("QlockSettings", android.content.Context.MODE_PRIVATE)

    var isScheduleEnabled by remember {
        mutableStateOf(prefs.getBoolean("schedule_enabled", false))
    }
    var startHour by remember { mutableStateOf(prefs.getInt("start_hour", 7)) }
    var startMinute by remember { mutableStateOf(prefs.getInt("start_minute", 0)) }
    var endHour by remember { mutableStateOf(prefs.getInt("end_hour", 22)) }
    var endMinute by remember { mutableStateOf(prefs.getInt("end_minute", 0)) }

    var ipAddress by remember { mutableStateOf(prefs.getString("ws_ip", "192.168.3.210") ?: "192.168.3.210") }
    var port by remember { mutableStateOf(prefs.getInt("ws_port", 81).toString()) }
    var ipError by remember { mutableStateOf(false) }
    var portError by remember { mutableStateOf(false) }

    var currentMode by remember { mutableStateOf("CLOCK") }
    val colorViewModel: ColorViewModel = viewModel()

    // Beobachte currentSettings StateFlow statt messages SharedFlow
    val currentSettingsMsg by webSocketManager.currentSettings.collectAsState()

    LaunchedEffect(currentSettingsMsg) {
        currentSettingsMsg?.let { msg ->
            println("SettingsScreen received: $msg")
            if (msg.startsWith("SETTINGS:")) {
                val parts = msg.removePrefix("SETTINGS:").split(",").map { it.trim() }
                println("SettingsScreen parts: $parts size=${parts.size}")
                if (parts.size >= 5) {
                    val mode = parts[0]
                    val r = parts[1].toIntOrNull() ?: 255
                    val g = parts[2].toIntOrNull() ?: 0
                    val b = parts[3].toIntOrNull() ?: 0
                    val brightnessValue = parts[4].toFloatOrNull() ?: 255f
                    println("SettingsScreen parsed: mode=$mode r=$r g=$g b=$b brightness=$brightnessValue")
                    currentMode = mode
                    colorViewModel.updateColor(Color(r / 255f, g / 255f, b / 255f))
                    colorViewModel.updateBrightness(brightnessValue)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Einstellungen") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_revert),
                            contentDescription = "Zurück"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Allgemeine Einstellungen",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // IP/Port Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Verbindungsdaten", style = MaterialTheme.typography.titleMedium)
                    androidx.compose.material3.OutlinedTextField(
                        value = ipAddress,
                        onValueChange = {
                            ipAddress = it
                            ipError = !Regex("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$").matches(it)
                        },
                        label = { Text("IP-Adresse") },
                        isError = ipError,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (ipError) Text("Ungültige IP-Adresse", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    androidx.compose.material3.OutlinedTextField(
                        value = port,
                        onValueChange = {
                            port = it.filter { c -> c.isDigit() }
                            portError = port.isEmpty() || port.toIntOrNull() == null || port.toInt() !in 1..65535
                        },
                        label = { Text("Port") },
                        isError = portError,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (portError) Text("Ungültiger Port (1-65535)", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Button(
                        onClick = {
                            if (!ipError && !portError) {
                                prefs.edit().putString("ws_ip", ipAddress).putInt("ws_port", port.toInt()).apply()
                                webSocketManager.disconnect()
                                webSocketManager.connect(ipAddress, port.toInt())
                            }
                        },
                        enabled = !ipError && !portError,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Übernehmen & neu verbinden")
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Zeitplan aktivieren", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = isScheduleEnabled,
                            onCheckedChange = { enabled ->
                                isScheduleEnabled = enabled
                                prefs.edit().putBoolean("schedule_enabled", enabled).apply()
                                val message = if (enabled) {
                                    "SCHEDULE:ON,$startHour:$startMinute,$endHour:$endMinute"
                                } else {
                                    "SCHEDULE:OFF"
                                }
                                webSocketManager.sendMessage(message)
                            }
                        )
                    }

                    if (isScheduleEnabled) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Startzeit", style = MaterialTheme.typography.bodyMedium)
                                Button(
                                    onClick = {
                                        TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                startHour = hour
                                                startMinute = minute
                                                prefs.edit()
                                                    .putInt("start_hour", hour)
                                                    .putInt("start_minute", minute)
                                                    .apply()
                                                webSocketManager.sendMessage(
                                                    "SCHEDULE:ON,$startHour:$startMinute,$endHour:$endMinute"
                                                )
                                            },
                                            startHour,
                                            startMinute,
                                            true
                                        ).show()
                                    },
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Icon(
                                        painter = painterResource(id = android.R.drawable.ic_menu_recent_history),
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                    Text(String.format("%02d:%02d", startHour, startMinute))
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Endzeit", style = MaterialTheme.typography.bodyMedium)
                                Button(
                                    onClick = {
                                        TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                endHour = hour
                                                endMinute = minute
                                                prefs.edit()
                                                    .putInt("end_hour", hour)
                                                    .putInt("end_minute", minute)
                                                    .apply()
                                                webSocketManager.sendMessage(
                                                    "SCHEDULE:ON,$startHour:$startMinute,$endHour:$endMinute"
                                                )
                                            },
                                            endHour,
                                            endMinute,
                                            true
                                        ).show()
                                    },
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Icon(
                                        painter = painterResource(id = android.R.drawable.ic_menu_recent_history),
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                    Text(String.format("%02d:%02d", endHour, endMinute))
                                }
                            }
                            Text(
                                text = "Die Uhr ist aktiv von ${String.format("%02d:%02d", startHour, startMinute)} bis ${String.format("%02d:%02d", endHour, endMinute)}.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Modus-Auswahl
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Modus wählen", style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val modes = listOf("CLOCK", "DIGITAL", "MATRIX", "TEMPERATURE")
                        modes.forEach { mode ->
                            Button(
                                onClick = {
                                    currentMode = mode
                                    webSocketManager.sendMessage("SET_MODE:$mode")
                                },
                                colors = if (currentMode == mode)
                                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                else
                                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text(mode)
                            }
                        }
                    }
                }
            }
            // Farb- und Helligkeitssteuerung
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Farbe wählen", style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val colors = listOf(
                            Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Cyan, Color.Magenta, Color.White
                        )
                        colors.forEach { color ->
                            Button(
                                onClick = { colorViewModel.updateColor(color) },
                                colors = ButtonDefaults.buttonColors(containerColor = color)
                            ) {
                                if (colorViewModel.selectedColor == color) Text("✓")
                            }
                        }
                    }
                    Text("Helligkeit: ${colorViewModel.brightness.toInt()}", style = MaterialTheme.typography.bodyMedium)
                    androidx.compose.material3.Slider(
                        value = colorViewModel.brightness,
                        onValueChange = { colorViewModel.updateBrightness(it) },
                        valueRange = 0f..255f,
                        steps = 10,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val message = if (isScheduleEnabled) {
                        "SCHEDULE:ON,$startHour:$startMinute,$endHour:$endMinute"
                    } else {
                        "SCHEDULE:OFF"
                    }
                    webSocketManager.sendMessage(message)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_send),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text("Einstellungen an Uhr senden")
            }
        }
    }
}