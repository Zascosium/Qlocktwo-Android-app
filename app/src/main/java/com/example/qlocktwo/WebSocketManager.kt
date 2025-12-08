package com.example.qlocktwo

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

    val messages = MutableSharedFlow<String>(replay = 1)
    private var session: DefaultWebSocketSession? = null

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val client = HttpClient(CIO) {
        install(WebSockets)
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    fun connect() {
        if (session?.isActive == true) return

        _connectionStatus.value = ConnectionStatus.CONNECTING

        scope.launch {
            try {
                client.webSocket(host = "192.168.3.210", port = 81, path = "/ws") {
                    session = this
                    _connectionStatus.value = ConnectionStatus.CONNECTED
                    println("WebSocket connection established.")
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            messages.emit(frame.readText())
                        }
                    }
                }
            } catch (e: Exception) {
                println("WebSocket connection failed: ${e.message}")
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
