package com.example.qlocktwo.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.qlocktwo.R
import com.example.qlocktwo.WebSocketManager
import java.util.Calendar

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_revert),
                            contentDescription = "Back"
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Active Hours Schedule",
                style = MaterialTheme.typography.headlineSmall
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Schedule")
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
                        Spacer(modifier = Modifier.height(8.dp))

                        // Start Time
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Start Time")
                            Button(onClick = {
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
                            }) {
                                Text(String.format("%02d:%02d", startHour, startMinute))
                            }
                        }

                        // End Time
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("End Time")
                            Button(onClick = {
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
                            }) {
                                Text(String.format("%02d:%02d", endHour, endMinute))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Clock will be active from ${String.format("%02d:%02d", startHour, startMinute)} to ${String.format("%02d:%02d", endHour, endMinute)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send Settings to Clock")
            }
        }
    }
}