package com.example.qlocktwo.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

@Composable
fun MatrixScreen(
    colorViewModel: ColorViewModel,
    webSocketManager: WebSocketManager
) {
    val matrix = listOf(
        "ESKISTAFÜNF",
        "ZEHNJEPSORM",
        "AFAXZWANZIG",
        "DREIVIERTEL",
        "VORFUNKNACH",
        "HALBAELFÜNF",
        "EINSXAMZWEI",
        "DREIPMJVIER",
        "SECHSNLACHT",
        "SIEBENZWÖLF",
        "ZEHNEUNKUHR"
    )

    // Track color for each position (row, col) -> Color
    var letterColors by remember { mutableStateOf(mapOf<Pair<Int, Int>, Color>()) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CommonControls(
            modifier = Modifier.padding(top = 48.dp),
            colorViewModel = colorViewModel
            // onSendMessage removed - color changes don't send to WebSocket
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            InteractiveMatrix(
                matrix = matrix,
                letterColors = letterColors,
                currentColor = colorViewModel.selectedColor,
                onToggle = { row, col ->
                    val position = row to col
                    val existingColor = letterColors[position]

                    if (existingColor == colorViewModel.selectedColor) {
                        // Same color - toggle off
                        letterColors = letterColors - position
                        // Send clear command to ESP32
                        webSocketManager.sendMessage("MATRIX_CLEAR:$row,$col")
                    } else {
                        // No color or different color - set to current color
                        val newColor = colorViewModel.selectedColor
                        letterColors = letterColors + (position to newColor)

                        // Send color update to ESP32
                        val r = (newColor.red * 255).toInt()
                        val g = (newColor.green * 255).toInt()
                        val b = (newColor.blue * 255).toInt()
                        val brightness = colorViewModel.brightness.toInt()
                        webSocketManager.sendMessage("MATRIX_SET:$row,$col,$r,$g,$b,$brightness")
                    }
                }
            )
        }
    }
}

@Composable
fun InteractiveMatrix(
    matrix: List<String>,
    letterColors: Map<Pair<Int, Int>, Color>,
    currentColor: Color,
    onToggle: (Int, Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        matrix.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEachIndexed { colIndex, char ->
                    val letterColor = letterColors[rowIndex to colIndex]
                    ToggleableLetterBox(
                        letter = char,
                        letterColor = letterColor,
                        onClick = { onToggle(rowIndex, colIndex) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ToggleableLetterBox(
    letter: Char,
    letterColor: Color?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = letterColor ?: Color(0xFF333333)
        )
    }
}