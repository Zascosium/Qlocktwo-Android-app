package com.example.qlocktwo

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

class WebSocketManager {

    val messages = MutableSharedFlow<String>(replay = 0)
    private var session: DefaultWebSocketSession? = null

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    // StateFlow für aktuelle Settings, damit mehrere Komponenten darauf zugreifen können
    private val _currentSettings = MutableStateFlow<String?>(null)
    val currentSettings: StateFlow<String?> = _currentSettings.asStateFlow()

    private val client = HttpClient(CIO) {
        install(WebSockets)
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    private var currentHost: String = "192.168.3.210"
    private var currentPort: Int = 81

    fun connectWithPrefs(context: Context) {
        val prefs = context.getSharedPreferences("QlockSettings", Context.MODE_PRIVATE)
        val host = prefs.getString("ws_ip", "192.168.3.210") ?: "192.168.3.210"
        val port = prefs.getInt("ws_port", 81)
        connect(host, port)
    }

    fun connect(host: String = currentHost, port: Int = currentPort) {
        if (session?.isActive == true && host == currentHost && port == currentPort) return

        currentHost = host
        currentPort = port
        _connectionStatus.value = ConnectionStatus.CONNECTING

        scope.launch {
            try {
                client.webSocket(host = host, port = port, path = "/ws") {
                    session = this
                    _connectionStatus.value = ConnectionStatus.CONNECTED
                    println("WebSocket connection established.")
                    // Nach Verbindungsaufbau Einstellungen anfordern
                    send(Frame.Text("GET_SETTINGS"))
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val msg = frame.readText()
                            println("WebSocket received: $msg")

                            // Settings separat im StateFlow speichern
                            if (msg.startsWith("SETTINGS:")) {
                                _currentSettings.value = msg
                            }

                            messages.emit(msg)
                        }
                    }
                }
            } catch (e: Exception) {
                println("WebSocket connection failed: \\${e.message}")
                _connectionStatus.value = ConnectionStatus.ERROR
            } finally {
                println("WebSocket connection closed.")
                session = null
                if (_connectionStatus.value != ConnectionStatus.ERROR) {
                    _connectionStatus.value = ConnectionStatus.DISCONNECTED
                }
            }
        }
    }

    fun sendMessage(message: String) {
        scope.launch {
            if (session?.isActive == true) {
                println("Sending message: $message")
                session?.send(Frame.Text(message))
            } else {
                println("Cannot send message, session is not active.")
            }
        }
    }

    fun disconnect() {
        scope.launch {
            session?.close()
            session = null
            _connectionStatus.value = ConnectionStatus.DISCONNECTED
            println("WebSocket disconnected.")
        }
    }
}
