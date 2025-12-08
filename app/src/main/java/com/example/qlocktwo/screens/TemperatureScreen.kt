package com.example.qlocktwo.screens

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
fun TemperatureScreen(
    colorViewModel: ColorViewModel,
    webSocketManager: WebSocketManager
) {
    var temperature by remember { mutableIntStateOf(20) }
    val lastMessage by webSocketManager.messages.collectAsState(initial = "")

    // Parse temperature from WebSocket messages
    LaunchedEffect(lastMessage) {
        if (lastMessage.startsWith("TEMP:")) {
            val temp = lastMessage.removePrefix("TEMP:").toIntOrNull()
            if (temp != null) {
                temperature = temp
            }
        }
    }

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

    val digit1 = if (temperature >= 10) temperature / 10 else -1  // -1 means no digit
    val digit2 = temperature % 10

    val pixelMatrix = createTemperatureMatrix(digit1, digit2)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CommonControls(
            modifier = Modifier.padding(top = 48.dp),
            colorViewModel = colorViewModel,
            onSendMessage = webSocketManager::sendMessage
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            TemperatureMatrix(
                matrix = matrix,
                pixelMatrix = pixelMatrix,
                color = colorViewModel.selectedColor
            )
        }
    }
}

@Composable
fun TemperatureMatrix(
    matrix: List<String>,
    pixelMatrix: Array<BooleanArray>,
    color: Color
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
                    val isOn = pixelMatrix[rowIndex][colIndex]
                    TemperatureLetterBox(
                        letter = char,
                        isOn = isOn,
                        color = color,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun TemperatureLetterBox(
    letter: Char,
    isOn: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isOn) color else Color(0xFF333333)
        )
    }
}

fun createTemperatureMatrix(digit1: Int, digit2: Int): Array<BooleanArray> {
    val matrix = Array(11) { BooleanArray(11) { false } }

    if (digit1 >= 0) {
        // Two digits: draw first digit at columns 0-4, second at 6-10
        drawTempDigit(matrix, digit1, 0)
        drawTempDigit(matrix, digit2, 6)
    } else {
        // One digit: draw centered at columns 1-5
        drawTempDigit(matrix, digit2, 1)
    }

    // Draw degree symbol at column 7 or 8 (depending on number of digits)
    val degreeCol = if (digit1 >= 0) 0 else 7  // Position after the digits
    if (degreeCol > 0) {
        drawDegreeSymbol(matrix, degreeCol)
    }

    return matrix
}

fun drawTempDigit(matrix: Array<BooleanArray>, digit: Int, startCol: Int) {
    val digitPattern = getTempDigitPattern(digit)

    // Center the digit vertically (start at row 2, use 7 rows)
    val startRow = 2

    digitPattern.forEachIndexed { row, line ->
        line.forEachIndexed { col, pixel ->
            if (pixel && startCol + col < 11) {
                matrix[startRow + row][startCol + col] = true
            }
        }
    }
}

fun drawDegreeSymbol(matrix: Array<BooleanArray>, startCol: Int) {
    // Small degree circle (3x3) positioned at top of digits
    val startRow = 2

    // Draw a small circle
    if (startCol + 2 < 11) {
        matrix[startRow][startCol] = true
        matrix[startRow][startCol + 1] = true
        matrix[startRow][startCol + 2] = true
        matrix[startRow + 1][startCol] = true
        matrix[startRow + 1][startCol + 2] = true
        matrix[startRow + 2][startCol] = true
        matrix[startRow + 2][startCol + 1] = true
        matrix[startRow + 2][startCol + 2] = true
    }
}

fun getTempDigitPattern(digit: Int): Array<BooleanArray> {
    // 5x7 pixel patterns for digits 0-9 (reusing from DigitalClockScreen logic)
    return when (digit) {
        0 -> arrayOf(
            booleanArrayOf(true, true, true, true, true),
            booleanArrayOf(true, false, false, false, true),
            booleanArrayOf(true, false, false, false, true),
            booleanArrayOf(true, false, false, false, true),
            booleanArrayOf(true, false, false, false, true),
            booleanArrayOf(true, false, false, false, true),
            booleanArrayOf(true, true, true, true, true)
        )
        1 -> arrayOf(
            booleanArrayOf(false, false, true, false, false),
            booleanArrayOf(false, true, true, false, false),
            booleanArrayOf(false, false, true, false, false),
            booleanArrayOf(false, false, true, false, false),
            booleanArrayOf(false, false, true, false, false),
            booleanArrayOf(false, false, true, false, false),
            booleanArrayOf(false, true, true, true, false)
        )
        2 -> arrayOf(
            booleanArrayOf(true, true, true, true, true),
            booleanArrayOf(false, false, false, false, true),
            booleanArrayOf(false, false, false, false, true),
            booleanArrayOf(true, true, true, true, true),
            booleanArrayOf(true, false, false, false, false),
            booleanArrayOf(true, false, false, false, false),
            booleanArrayOf(true, true, true, true, true)
        )
        3 -> arrayOf(
            booleanArrayOf(true, true, true, true, true),
            booleanArrayOf(false, false, false, false, true),
            booleanArrayOf(false, false, false, false, true),
            booleanArrayOf(true, true, true, true, true),
            booleanArrayOf(false, false, false, false, true),
            booleanArrayOf(false, false, false, false, true),
            booleanArrayOf(true, true, true, true, true)
        )
        4 -> arrayOf(
            booleanArrayOf(true, false, false, false, true),
            booleanArrayOf(true, false, false, false, true),
            booleanArrayOf(true, false, false, false, true),
            booleanArrayOf(true, true, true, true, true),
            booleanArrayOf(false, false, false, false, true),
            booleanArrayOf(false, false, false, false, true),
            booleanArrayOf(false, false, false, false, true)
        )
        5 -> arrayOf(
            booleanArrayOf(true, true, true, true, true),
            booleanArrayOf(true, false, false, false, false),
            booleanArrayOf(true, false, false, false, false),
            booleanArrayOf(true, true, true, true, true),
            booleanArrayOf(false, false, false, false, true),
            booleanArrayOf(false, false, false, false, true),
            booleanArrayOf(true, true, true, true, true)
        )
        6 -> arrayOf(
            booleanArrayOf(true, true, true, true, true),
            booleanArrayOf(true, false, false, false, false),
            booleanArrayOf(true, false, false, false, false),
            booleanArrayOf(true, true, true, true, true),
            booleanArrayOf(true, false, false, false, true),
            booleanArrayOf(true, false, false, false, true),
            booleanArrayOf(true, true, true, true, true)
        )
        7 -> arrayOf(
            booleanArrayOf(true, true, true, true, true),
            booleanArrayOf(false, false, false, false, true),
            booleanArrayOf(false, false, false, false, true),
            booleanArrayOf(false, false, false, true, false),
            booleanArrayOf(false, false, true, false, false),
            booleanArrayOf(false, false, true, false, false),
            booleanArrayOf(false, false, true, false, false)
        )
        8 -> arrayOf(
            booleanArrayOf(true, true, true, true, true),
            booleanArrayOf(true, false, false, false, true),
            booleanArrayOf(true, false, false, false, true),
            booleanArrayOf(true, true, true, true, true),
            booleanArrayOf(true, false, false, false, true),
            booleanArrayOf(true, false, false, false, true),
            booleanArrayOf(true, true, true, true, true)
        )
        9 -> arrayOf(
            booleanArrayOf(true, true, true, true, true),
            booleanArrayOf(true, false, false, false, true),
            booleanArrayOf(true, false, false, false, true),
            booleanArrayOf(true, true, true, true, true),
            booleanArrayOf(false, false, false, false, true),
            booleanArrayOf(false, false, false, false, true),
            booleanArrayOf(true, true, true, true, true)
        )
        else -> Array(7) { BooleanArray(5) { false } }
    }
}