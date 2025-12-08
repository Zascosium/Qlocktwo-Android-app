package com.example.qlocktwo.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.qlocktwo.R
import com.example.qlocktwo.WebSocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ModeWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_CYCLE_MODE = "com.example.qlocktwo.ACTION_CYCLE_MODE"
        const val PREFS_NAME = "ModeWidgetPrefs"
        const val PREF_CURRENT_MODE = "current_mode"

        const val MODE_CLOCK = 0
        const val MODE_DIGITAL = 1
        const val MODE_TEMP = 2
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_CYCLE_MODE) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val currentMode = prefs.getInt(PREF_CURRENT_MODE, MODE_CLOCK)

            // Cycle to next mode
            val nextMode = when (currentMode) {
                MODE_CLOCK -> MODE_DIGITAL
                MODE_DIGITAL -> MODE_TEMP
                MODE_TEMP -> MODE_CLOCK
                else -> MODE_CLOCK
            }

            // Save new mode
            prefs.edit().putInt(PREF_CURRENT_MODE, nextMode).apply()

            // Send mode change to ESP32
            val webSocketManager = WebSocketManager()
            val scope = CoroutineScope(Dispatchers.IO)

            scope.launch {
                webSocketManager.connect()
                // Wait a bit for connection to establish
                delay(500)

                val modeMessage = when (nextMode) {
                    MODE_CLOCK -> "MODE:CLOCK"
                    MODE_DIGITAL -> "MODE:DIGITAL"
                    MODE_TEMP -> "MODE:TEMP"
                    else -> "MODE:CLOCK"
                }
                webSocketManager.sendMessage(modeMessage)

                // Disconnect after sending
                delay(1000)
                webSocketManager.disconnect()
            }

            // Update all widgets
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, ModeWidgetProvider::class.java)
            )
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.mode_widget)

        // Get current mode
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentMode = prefs.getInt(PREF_CURRENT_MODE, MODE_CLOCK)

        // Set icon based on current mode
        val iconRes = when (currentMode) {
            MODE_CLOCK -> R.drawable.ic_clock
            MODE_DIGITAL -> R.drawable.ic_digital_clock
            MODE_TEMP -> R.drawable.ic_temperature
            else -> R.drawable.ic_clock
        }
        views.setImageViewResource(R.id.mode_icon, iconRes)

        // Set up click intent to cycle mode
        val intent = Intent(context, ModeWidgetProvider::class.java).apply {
            action = ACTION_CYCLE_MODE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.mode_icon, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
