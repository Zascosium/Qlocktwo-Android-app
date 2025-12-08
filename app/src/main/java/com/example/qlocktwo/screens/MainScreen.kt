package com.example.qlocktwo.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.qlocktwo.R
import com.example.qlocktwo.WebSocketManager
import com.example.qlocktwo.navigation.Screen
import com.example.qlocktwo.viewmodels.ColorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    webSocketManager: WebSocketManager,
    colorViewModel: ColorViewModel
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> webSocketManager.connect()
                Lifecycle.Event.ON_STOP -> webSocketManager.disconnect()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val navController = rememberNavController()
    val screens = listOf(
        Screen.Clock,
        Screen.DigitalClock,
        Screen.Temperature,
        Screen.Alarm,
        Screen.Matrix
    )

    val connectionStatus by webSocketManager.connectionStatus.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Send mode change message to ESP32 when screen changes
    LaunchedEffect(currentRoute) {
        currentRoute?.let { route ->
            val modeMessage = when (route) {
                Screen.Clock.route -> "MODE:CLOCK"
                Screen.DigitalClock.route -> "MODE:DIGITAL"
                Screen.Temperature.route -> "MODE:TEMP"
                Screen.Alarm.route -> "MODE:ALARM"
                Screen.Matrix.route -> "MODE:MATRIX"
                else -> null
            }
            modeMessage?.let { webSocketManager.sendMessage(it) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    ConnectionStatusIndicator(status = connectionStatus)
                    IconButton(onClick = {
                        navController.navigate("settings")
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_settings),
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val currentDestination = navBackStackEntry?.destination
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(painterResource(id = screen.icon), contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Clock.route, Modifier.padding(innerPadding)) {
            composable(Screen.Clock.route) {
                val lastMessage by webSocketManager.messages.collectAsState(initial = "")
                ClockScreen(
                    lastMessage = lastMessage,
                    onSendMessage = webSocketManager::sendMessage,
                    colorViewModel = colorViewModel
                )
            }
            composable(Screen.DigitalClock.route) {
                DigitalClockScreen(
                    colorViewModel = colorViewModel,
                    webSocketManager = webSocketManager
                )
            }
            composable(Screen.Temperature.route) {
                TemperatureScreen(
                    colorViewModel = colorViewModel,
                    webSocketManager = webSocketManager
                )
            }
            composable(Screen.Alarm.route) {
                AlarmScreen(
                    colorViewModel = colorViewModel,
                    webSocketManager = webSocketManager
                )
            }
            composable(Screen.Matrix.route) {
                MatrixScreen(
                    colorViewModel = colorViewModel,
                    webSocketManager = webSocketManager
                )
            }
            composable("settings") {
                SettingsScreen(
                    webSocketManager = webSocketManager,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}