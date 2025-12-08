package com.example.qlocktwo.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.qlocktwo.ui.theme.QlocktwoTheme

@Composable
fun Esp32Input(modifier: Modifier = Modifier, lastMessage: String, onSendMessage: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Enter data for ESP32") }
        )
        Button(onClick = { onSendMessage(text) }) {
            Text("Send")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Received: $lastMessage")
    }
}

@Preview(showBackground = true)
@Composable
fun Esp32InputPreview() {
    QlocktwoTheme {
        Esp32Input(lastMessage = "Preview message", onSendMessage = { })
    }
}
