package com.example.qlocktwo.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel

class ColorViewModel : ViewModel() {
    var selectedColor by mutableStateOf(Color.White)
        private set

    var brightness by mutableFloatStateOf(255f)
        private set

    fun updateColor(color: Color) {
        selectedColor = color
    }

    fun updateBrightness(value: Float) {
        brightness = value
    }
}
