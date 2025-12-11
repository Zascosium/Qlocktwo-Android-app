package com.example.qlocktwo.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qlocktwo.WebSocketManager
import com.example.qlocktwo.viewmodels.ColorViewModel
import kotlinx.coroutines.delay

@Composable
fun AlarmScreen(
    colorViewModel: ColorViewModel,
    webSocketManager: WebSocketManager
) {
    var hours by remember { mutableIntStateOf(0) }
    var minutes by remember { mutableIntStateOf(5) }
    var seconds by remember { mutableIntStateOf(0) }

    var isRunning by remember { mutableStateOf(false) }
    var remainingSeconds by remember { mutableIntStateOf(0) }

    // Countdown effect - runs every second when timer is active
    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        } else if (isRunning && remainingSeconds == 0) {
            isRunning = false
            // Timer finished
        }
    }

    val totalSeconds = hours * 3600 + minutes * 60 + seconds

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        CommonControls(
            modifier = Modifier.padding(top = 48.dp),
            colorViewModel = colorViewModel,
            onSendMessage = webSocketManager::sendMessage
        )

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Large timer display
            Text(
                text = "SET TIMER",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Calculate display time - show remaining time when running, set time when stopped
            val displayHours = if (isRunning) remainingSeconds / 3600 else hours
            val displayMinutes = if (isRunning) (remainingSeconds % 3600) / 60 else minutes
            val displaySeconds = if (isRunning) remainingSeconds % 60 else seconds

            Text(
                text = String.format("%02d:%02d:%02d", displayHours, displayMinutes, displaySeconds),
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = colorViewModel.selectedColor
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Quick preset buttons
            Text(
                text = "Quick Presets",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PresetButton("1 min") {
                    hours = 0
                    minutes = 1
                    seconds = 0
                }
                PresetButton("5 min") {
                    hours = 0
                    minutes = 5
                    seconds = 0
                }
                PresetButton("10 min") {
                    hours = 0
                    minutes = 10
                    seconds = 0
                }
                PresetButton("30 min") {
                    hours = 0
                    minutes = 30
                    seconds = 0
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Custom time adjusters
            Text(
                text = "Custom Time",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TimeAdjuster(
                    label = "Hours",
                    value = hours,
                    onIncrement = { hours = (hours + 1).coerceIn(0, 23) },
                    onDecrement = { hours = (hours - 1).coerceIn(0, 23) }
                )
                TimeAdjuster(
                    label = "Minutes",
                    value = minutes,
                    onIncrement = { minutes = (minutes + 1).coerceIn(0, 59) },
                    onDecrement = { minutes = (minutes - 1).coerceIn(0, 59) }
                )
                TimeAdjuster(
                    label = "Seconds",
                    value = seconds,
                    onIncrement = { seconds = (seconds + 1).coerceIn(0, 59) },
                    onDecrement = { seconds = (seconds - 1).coerceIn(0, 59) }
                )
            }
        }

        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    if (!isRunning && totalSeconds > 0) {
                        remainingSeconds = totalSeconds
                        isRunning = true
                        webSocketManager.sendMessage("TIMER:START:$totalSeconds")
                    }
                },
                enabled = totalSeconds > 0 && !isRunning,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorViewModel.selectedColor
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Start")
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = {
                    isRunning = false
                    webSocketManager.sendMessage("TIMER:STOP")
                },
                enabled = isRunning,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Stop")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Stop")
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = {
                    isRunning = false
                    remainingSeconds = 0
                    hours = 0
                    minutes = 0
                    seconds = 0
                    webSocketManager.sendMessage("TIMER:RESET")
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Reset")
            }
        }
    }
}

@Composable
fun PresetButton(
    text: String,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.width(80.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp
        )
    }
}

@Composable
fun TimeAdjuster(
    label: String,
    value: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(4.dp))

        IconButton(
            onClick = onIncrement,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Increase")
        }

        Text(
            text = String.format("%02d", value),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        IconButton(
            onClick = onDecrement,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Decrease",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

