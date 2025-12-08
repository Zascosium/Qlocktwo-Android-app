package com.example.qlocktwo.navigation

import androidx.annotation.DrawableRes
import com.example.qlocktwo.R

sealed class Screen(val route: String, @DrawableRes val icon: Int, val title: String) {
    object Clock : Screen("clock", R.drawable.ic_clock, "Clock")
    object DigitalClock : Screen("digital_clock", R.drawable.ic_digital_clock, "Digital")
    object Temperature : Screen("temperature", R.drawable.ic_temperature, "Temp")
    object Alarm : Screen("alarm", R.drawable.ic_alarm, "Alarm")
    object Matrix : Screen("matrix", R.drawable.ic_matrix, "Matrix")
}
