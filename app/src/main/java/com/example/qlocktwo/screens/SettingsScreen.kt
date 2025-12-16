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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    var startHour by remember { mutableStateOf(prefs.getInt("start_hour", 21)) }
    var startMinute by remember { mutableStateOf(prefs.getInt("start_minute", 45)) }
    var endHour by remember { mutableStateOf(prefs.getInt("end_hour", 6)) }
    var endMinute by remember { mutableStateOf(prefs.getInt("end_minute", 35)) }

    var ipAddress by remember { mutableStateOf(prefs.getString("ws_ip", "192.168.3.219") ?: "192.168.3.219") }
    var port by remember { mutableStateOf(prefs.getInt("ws_port", 81).toString()) }
    var ipError by remember { mutableStateOf(false) }
    var portError by remember { mutableStateOf(false) }

    // Listen for schedule updates from ESP32
    LaunchedEffect(webSocketManager) {
        webSocketManager.currentSchedule.collect { scheduleSettings ->
            scheduleSettings?.let {
                isScheduleEnabled = it.enabled
                startHour = it.startHour
                startMinute = it.startMinute
                endHour = it.endHour
                endMinute = it.endMinute

                // Save to SharedPreferences
                prefs.edit()
                    .putBoolean("schedule_enabled", it.enabled)
                    .putInt("start_hour", it.startHour)
                    .putInt("start_minute", it.startMinute)
                    .putInt("end_hour", it.endHour)
                    .putInt("end_minute", it.endMinute)
                    .apply()

                println("SettingsScreen: Schedule updated from ESP32 - enabled=${it.enabled}, ${it.startHour}:${it.startMinute} - ${it.endHour}:${it.endMinute}")
            }
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Allgemeine Einstellungen",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 4.dp)
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
        }
    }
}