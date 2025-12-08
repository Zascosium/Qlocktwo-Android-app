package com.example.qlocktwo.viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel

class ColorViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs: SharedPreferences = application.getSharedPreferences("color_prefs", Context.MODE_PRIVATE)

    var selectedColor by mutableStateOf(Color.White)
        private set

    var brightness by mutableFloatStateOf(255f)
        private set

    private fun colorToInt(color: Color): Int {
        val a = (color.alpha * 255).toInt() shl 24
        val r = (color.red * 255).toInt() shl 16
        val g = (color.green * 255).toInt() shl 8
        val b = (color.blue * 255).toInt()
        return a or r or g or b
    }

    private fun intToColor(value: Int): Color {
        val a = ((value shr 24) and 0xFF) / 255f
        val r = ((value shr 16) and 0xFF) / 255f
        val g = ((value shr 8) and 0xFF) / 255f
        val b = (value and 0xFF) / 255f
        return Color(r, g, b, a)
    }

    init {
        // Farbe beim Start laden
        val colorInt = prefs.getInt("selected_color", colorToInt(Color.White))
        selectedColor = intToColor(colorInt)
        // Helligkeit beim Start laden
        brightness = prefs.getFloat("brightness", 255f)
    }

    fun updateColor(color: Color) {
        selectedColor = color
        // Farbe speichern
        prefs.edit().putInt("selected_color", colorToInt(color)).apply()
    }

    fun updateBrightness(value: Float) {
        brightness = value
        // Helligkeit speichern
        prefs.edit().putFloat("brightness", value).apply()
    }
}
