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
import kotlinx.coroutines.delay
import java.time.LocalTime

@Composable
fun DigitalClockScreen(
    colorViewModel: ColorViewModel,
    webSocketManager: WebSocketManager
) {
    var currentTime by remember { mutableStateOf(LocalTime.now()) }

    // Update time every second
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(1000)
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

    val minutes = currentTime.minute
    val digit1 = minutes / 10  // Tens digit
    val digit2 = minutes % 10  // Ones digit

    val pixelMatrix = createDigitalClockMatrix(digit1, digit2)

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
            DigitalMatrix(
                matrix = matrix,
                pixelMatrix = pixelMatrix,
                color = colorViewModel.selectedColor
            )
        }
    }
}

@Composable
fun DigitalMatrix(
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
                    DigitalLetterBox(
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
fun DigitalLetterBox(
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

fun createDigitalClockMatrix(digit1: Int, digit2: Int): Array<BooleanArray> {
    val matrix = Array(11) { BooleanArray(11) { false } }

    // Draw first digit (left side, columns 0-4)
    drawDigit(matrix, digit1, 0)

    // Draw second digit (right side, columns 6-10)
    drawDigit(matrix, digit2, 6)

    return matrix
}

fun drawDigit(matrix: Array<BooleanArray>, digit: Int, startCol: Int) {
    val digitPattern = getDigitPattern(digit)

    // Center the digit vertically (start at row 2, use 7 rows)
    val startRow = 2

    digitPattern.forEachIndexed { row, line ->
        line.forEachIndexed { col, pixel ->
            if (pixel) {
                matrix[startRow + row][startCol + col] = true
            }
        }
    }
}

fun getDigitPattern(digit: Int): Array<BooleanArray> {
    // 5x7 pixel patterns for digits 0-9
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

