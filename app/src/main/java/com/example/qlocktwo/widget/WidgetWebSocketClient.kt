package com.example.qlocktwo.widget

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

/**
 * Lightweight WebSocket client for widget operations.
 * Designed for connect→send→disconnect pattern.
 */
class WidgetWebSocketClient(private val context: Context) {

    /**
     * Sends a mode command to ESP32.
     * @param modeMessage The message to send (e.g., "MODE:CLOCK")
     * @return true if successful, false otherwise (silent failure)
     */
    suspend fun sendModeCommand(modeMessage: String): Boolean {
        // Read connection settings from SharedPreferences
        val prefs = context.getSharedPreferences("QlockSettings", Context.MODE_PRIVATE)
        val host = prefs.getString("ws_ip", "192.168.3.219") ?: "192.168.3.219"
        val port = prefs.getInt("ws_port", 81)

        val client = HttpClient(CIO) {
            install(WebSockets)
            engine {
                // Configure timeouts
                requestTimeout = 5000 // 5 seconds
            }
        }

        return try {
            withTimeout(5000L) { // 5 second timeout for entire operation
                client.webSocket(host = host, port = port, path = "/ws") {
                    // Connection established, send message
                    send(Frame.Text(modeMessage))
                    println("Widget: Sent message: $modeMessage to $host:$port")

                    // Close connection gracefully
                    close()
                }
            }
            true // Success
        } catch (e: TimeoutCancellationException) {
            println("Widget: Connection timeout to $host:$port")
            false
        } catch (e: Exception) {
            println("Widget: Failed to send mode command: ${e.message}")
            false
        } finally {
            try {
                client.close()
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }
}
