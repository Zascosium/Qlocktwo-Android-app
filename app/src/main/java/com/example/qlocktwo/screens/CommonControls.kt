package com.example.qlocktwo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qlocktwo.ConnectionStatus
import com.example.qlocktwo.viewmodels.ColorViewModel
import com.godaddy.android.colorpicker.harmony.ColorHarmonyMode
import com.godaddy.android.colorpicker.harmony.HarmonyColorPicker

@Composable
fun CommonControls(
    colorViewModel: ColorViewModel,
    modifier: Modifier = Modifier,
    onSendMessage: ((String) -> Unit)? = null
) {
    var showDialog by remember { mutableStateOf(false) }

    // Slider value that responds to both user input AND ViewModel updates
    var sliderValue by remember { mutableFloatStateOf(colorViewModel.brightness) }

    // Track if user is actively dragging the slider
    var isDragging by remember { mutableStateOf(false) }

    // Sync slider with ViewModel when ViewModel changes (e.g., from WebSocket)
    LaunchedEffect(colorViewModel.brightness) {
        if (!isDragging) {
            sliderValue = colorViewModel.brightness
        }
    }

    // Debounce slider changes and send to ViewModel + WebSocket
    LaunchedEffect(sliderValue) {
        if (isDragging) {
            delay(50)
            colorViewModel.updateBrightness(sliderValue)
            onSendMessage?.invoke(formatColorMessage(colorViewModel.selectedColor, sliderValue))
        }
    }

    if (showDialog) {
        ColorPickerDialog(
            currentColor = colorViewModel.selectedColor,
            onColorSelected = { color ->
                colorViewModel.updateColor(color)
                // Send color update to ESP32
                onSendMessage?.invoke(formatColorMessage(color, colorViewModel.brightness))
            },
            onDismiss = { showDialog = false }
        )
    }

    Row(
        modifier = modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colorViewModel.selectedColor)
                .clickable { showDialog = true }
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Brightness Slider (without label)
        Slider(
            value = sliderValue,
            onValueChange = { newValue ->
                isDragging = true
                sliderValue = newValue
            },
            onValueChangeFinished = {
                isDragging = false
            },
            valueRange = 0f..255f,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White.copy(alpha = 0.7f)
            )
        )
    }
}

private fun formatColorMessage(color: Color, brightness: Float): String {
    val red = (color.red * 255).toInt()
    val green = (color.green * 255).toInt()
    val blue = (color.blue * 255).toInt()
    val brightnessInt = brightness.toInt()
    return "COLOR:$red,$green,$blue,$brightnessInt"
}

@Composable
private fun ColorPickerDialog(
    currentColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    // Convert Color to RGB values for sliders
    var red by remember { mutableFloatStateOf(currentColor.red * 255f) }
    var green by remember { mutableFloatStateOf(currentColor.green * 255f) }
    var blue by remember { mutableFloatStateOf(currentColor.blue * 255f) }

    // Sync the RGB sliders when color wheel changes
    var pickerColor by remember { mutableStateOf(currentColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Color") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 600.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Color Wheel
                HarmonyColorPicker(
                    modifier = Modifier.size(250.dp),
                    harmonyMode = ColorHarmonyMode.NONE,
                    onColorChanged = { hsvColor ->
                        val newColor = hsvColor.toColor()
                        pickerColor = newColor
                        red = newColor.red * 255f
                        green = newColor.green * 255f
                        blue = newColor.blue * 255f
                        onColorSelected(newColor)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Color Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(red / 255f, green / 255f, blue / 255f))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // RGB Sliders
                Text(
                    text = "RGB Values",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Red Slider
                RgbSlider(
                    label = "Red",
                    value = red,
                    color = Color.Red,
                    onValueChange = { newValue ->
                        red = newValue
                        val newColor = Color(red / 255f, green / 255f, blue / 255f)
                        onColorSelected(newColor)
                    }
                )

                // Green Slider
                RgbSlider(
                    label = "Green",
                    value = green,
                    color = Color.Green,
                    onValueChange = { newValue ->
                        green = newValue
                        val newColor = Color(red / 255f, green / 255f, blue / 255f)
                        onColorSelected(newColor)
                    }
                )

                // Blue Slider
                RgbSlider(
                    label = "Blue",
                    value = blue,
                    color = Color.Blue,
                    onValueChange = { newValue ->
                        blue = newValue
                        val newColor = Color(red / 255f, green / 255f, blue / 255f)
                        onColorSelected(newColor)
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun RgbSlider(
    label: String,
    value: Float,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                modifier = Modifier.width(60.dp)
            )
            Text(
                text = "${value.toInt()}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(40.dp)
            )
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = 0f..255f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = color,
                    activeTrackColor = color.copy(alpha = 0.7f)
                )
            )
        }
    }
}

@Composable
fun ConnectionStatusIndicator(
    status: ConnectionStatus,
    modifier: Modifier = Modifier
) {
    val statusColor = when (status) {
        ConnectionStatus.CONNECTED -> Color(0xFF4CAF50) // Green
        ConnectionStatus.CONNECTING -> Color(0xFFFFC107) // Amber
        ConnectionStatus.DISCONNECTED -> Color(0xFF9E9E9E) // Gray
        ConnectionStatus.ERROR -> Color(0xFFF44336) // Red
    }

    val statusText = when (status) {
        ConnectionStatus.CONNECTED -> "Connected"
        ConnectionStatus.CONNECTING -> "Connecting..."
        ConnectionStatus.DISCONNECTED -> "Disconnected"
        ConnectionStatus.ERROR -> "Error"
    }

    Row(
        modifier = modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(statusColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = statusText,
            fontSize = 12.sp,
            color = statusColor
        )
    }
}
