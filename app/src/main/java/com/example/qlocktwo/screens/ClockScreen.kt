package com.example.qlocktwo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qlocktwo.viewmodels.ColorViewModel
import kotlinx.coroutines.delay
import java.time.LocalTime

@Composable
fun ClockScreen(
    lastMessage: String,
    onSendMessage: (String) -> Unit,
    colorViewModel: ColorViewModel
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

    val highlightedPositions = getHighlightedPositions(currentTime)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CommonControls(
            modifier = Modifier.padding(top = 48.dp),
            colorViewModel = colorViewModel,
            onSendMessage = onSendMessage
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            WordClockMatrix(
                matrix = matrix,
                highlightedPositions = highlightedPositions,
                color = colorViewModel.selectedColor
            )
        }
    }
}

@Composable
fun WordClockMatrix(
    matrix: List<String>,
    highlightedPositions: Set<Pair<Int, Int>>,
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
                    val isHighlighted = highlightedPositions.contains(rowIndex to colIndex)
                    LetterBox(
                        letter = char,
                        isHighlighted = isHighlighted,
                        color = color,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun LetterBox(
    letter: Char,
    isHighlighted: Boolean,
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
            color = if (isHighlighted) color else Color(0xFF333333)
        )
    }
}

fun getHighlightedPositions(time: LocalTime): Set<Pair<Int, Int>> {
    val positions = mutableSetOf<Pair<Int, Int>>()

    // Always show "ES IST" (It is)
    // E S at row 0, columns 0-1
    positions.add(0 to 0)
    positions.add(0 to 1)
    // I S T at row 0, columns 3-5
    positions.add(0 to 3)
    positions.add(0 to 4)
    positions.add(0 to 5)

    val hour = time.hour % 12
    val minute = time.minute

    // Determine minute words
    when {
        minute in 0..4 -> {
            // Exact hour - show "UHR"
            // U H R at row 10, columns 8-10
            positions.add(10 to 8)
            positions.add(10 to 9)
            positions.add(10 to 10)
        }
        minute in 5..9 -> {
            // FÜNF NACH
            addFünfMinute(positions)
            addNach(positions)
        }
        minute in 10..14 -> {
            // ZEHN NACH
            addZehnMinute(positions)
            addNach(positions)
        }
        minute in 15..19 -> {
            // VIERTEL NACH
            addViertel(positions)
            addNach(positions)
        }
        minute in 20..24 -> {
            // ZWANZIG NACH
            addZwanzig(positions)
            addNach(positions)
        }
        minute in 25..29 -> {
            // FÜNF VOR HALB
            addFünfMinute(positions)
            addVor(positions)
            addHalb(positions)
        }
        minute in 30..34 -> {
            // HALB
            addHalb(positions)
        }
        minute in 35..39 -> {
            // FÜNF NACH HALB
            addFünfMinute(positions)
            addNach(positions)
            addHalb(positions)
        }
        minute in 40..44 -> {
            // ZWANZIG VOR
            addZwanzig(positions)
            addVor(positions)
        }
        minute in 45..49 -> {
            // VIERTEL VOR
            addViertel(positions)
            addVor(positions)
        }
        minute in 50..54 -> {
            // ZEHN VOR
            addZehnMinute(positions)
            addVor(positions)
        }
        minute in 55..59 -> {
            // FÜNF VOR
            addFünfMinute(positions)
            addVor(positions)
        }
    }

    // Determine which hour to show
    val displayHour = when {
        minute >= 25 -> (hour + 1) % 12
        else -> hour
    }

    // Add hour positions
    addHour(positions, if (displayHour == 0) 12 else displayHour)

    return positions
}

// Helper functions to add word positions
fun addFünfMinute(positions: MutableSet<Pair<Int, Int>>) {
    // FÜNF at row 0, columns 7-10
    positions.add(0 to 7)
    positions.add(0 to 8)
    positions.add(0 to 9)
    positions.add(0 to 10)
}

fun addZehnMinute(positions: MutableSet<Pair<Int, Int>>) {
    // ZEHN at row 1, columns 0-3
    positions.add(1 to 0)
    positions.add(1 to 1)
    positions.add(1 to 2)
    positions.add(1 to 3)
}

fun addZwanzig(positions: MutableSet<Pair<Int, Int>>) {
    // ZWANZIG at row 2, columns 4-10
    positions.add(2 to 4)
    positions.add(2 to 5)
    positions.add(2 to 6)
    positions.add(2 to 7)
    positions.add(2 to 8)
    positions.add(2 to 9)
    positions.add(2 to 10)
}

fun addViertel(positions: MutableSet<Pair<Int, Int>>) {
    // VIERTEL at row 3, columns 4-10
    positions.add(3 to 4)
    positions.add(3 to 5)
    positions.add(3 to 6)
    positions.add(3 to 7)
    positions.add(3 to 8)
    positions.add(3 to 9)
    positions.add(3 to 10)
}

fun addVor(positions: MutableSet<Pair<Int, Int>>) {
    // VOR at row 4, columns 0-2
    positions.add(4 to 0)
    positions.add(4 to 1)
    positions.add(4 to 2)
}

fun addNach(positions: MutableSet<Pair<Int, Int>>) {
    // NACH at row 4, columns 7-10
    positions.add(4 to 7)
    positions.add(4 to 8)
    positions.add(4 to 9)
    positions.add(4 to 10)
}

fun addHalb(positions: MutableSet<Pair<Int, Int>>) {
    // HALB at row 5, columns 0-3
    positions.add(5 to 0)
    positions.add(5 to 1)
    positions.add(5 to 2)
    positions.add(5 to 3)
}

fun addHour(positions: MutableSet<Pair<Int, Int>>, hour: Int) {
    when (hour) {
        1 -> {
            // EINS at row 6, columns 0-3
            positions.add(6 to 0)
            positions.add(6 to 1)
            positions.add(6 to 2)
            positions.add(6 to 3)
        }
        2 -> {
            // ZWEI at row 6, columns 7-10
            positions.add(6 to 7)
            positions.add(6 to 8)
            positions.add(6 to 9)
            positions.add(6 to 10)
        }
        3 -> {
            // DREI at row 7, columns 0-3
            positions.add(7 to 0)
            positions.add(7 to 1)
            positions.add(7 to 2)
            positions.add(7 to 3)
        }
        4 -> {
            // VIER at row 7, columns 7-10
            positions.add(7 to 7)
            positions.add(7 to 8)
            positions.add(7 to 9)
            positions.add(7 to 10)
        }
        5 -> {
            // FÜNF at row 5, columns 7-10
            positions.add(5 to 7)
            positions.add(5 to 8)
            positions.add(5 to 9)
            positions.add(5 to 10)
        }
        6 -> {
            // SECHS at row 8, columns 0-4
            positions.add(8 to 0)
            positions.add(8 to 1)
            positions.add(8 to 2)
            positions.add(8 to 3)
            positions.add(8 to 4)
        }
        7 -> {
            // SIEBEN at row 9, columns 0-5
            positions.add(9 to 0)
            positions.add(9 to 1)
            positions.add(9 to 2)
            positions.add(9 to 3)
            positions.add(9 to 4)
            positions.add(9 to 5)
        }
        8 -> {
            // ACHT at row 8, columns 7-10
            positions.add(8 to 7)
            positions.add(8 to 8)
            positions.add(8 to 9)
            positions.add(8 to 10)
        }
        9 -> {
            // NEUN at row 10, columns 4-7
            positions.add(10 to 4)
            positions.add(10 to 5)
            positions.add(10 to 6)
            positions.add(10 to 7)
        }
        10 -> {
            // ZEHN at row 10, columns 0-3
            positions.add(10 to 0)
            positions.add(10 to 1)
            positions.add(10 to 2)
            positions.add(10 to 3)
        }
        11 -> {
            // ELF at row 5, columns 4-6
            positions.add(5 to 4)
            positions.add(5 to 5)
            positions.add(5 to 6)
        }
        12 -> {
            // ZWÖLF at row 9, columns 6-10
            positions.add(9 to 6)
            positions.add(9 to 7)
            positions.add(9 to 8)
            positions.add(9 to 9)
            positions.add(9 to 10)
        }
    }
}

