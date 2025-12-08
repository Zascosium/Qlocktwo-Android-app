package com.example.qlocktwo.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ColorViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ColorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ColorViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

