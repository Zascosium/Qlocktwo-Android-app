# ESP32 WebSocket API Documentation

## Overview
This document describes the WebSocket communication protocol between the Android app and the ESP32 QLOCKTWO device.

**Connection Details:**
- Protocol: WebSocket (ws://)
- Default Port: 81
- Path: `/ws`
- Example: `ws://192.168.3.210:81/ws`

## Table of Contents

1. [Color and Brightness Control](#1-color-and-brightness-control)
2. [Temperature Display](#2-temperature-display)
3. [Timer Control](#3-timer-control)
4. [Matrix Control (Custom LED Patterns)](#4-matrix-control-custom-led-patterns)
5. [Settings Synchronization](#5-settings-synchronization)
6. [Mode Control](#6-mode-control)
7. [Schedule Control](#7-schedule-control)
8. [Screen-Specific Behavior](#screen-specific-behavior)
9. [Connection Lifecycle](#connection-lifecycle)
10. [Error Handling](#error-handling)
11. [Implementation Notes](#implementation-notes)
12. [Example Communication Flows](#example-communication-flows)

---

## Message Formats

### 1. Color and Brightness Control

#### App → ESP32: Set Color and Brightness
```
COLOR:R,G,B,brightness
```

**Parameters:**
- `R`: Red component (0-255)
- `G`: Green component (0-255)
- `B`: Blue component (0-255)
- `brightness`: Brightness level (0-255)

**Examples:**
```
COLOR:255,0,0,200        # Red at 200 brightness
COLOR:0,255,255,255      # Cyan at full brightness
COLOR:128,128,128,100    # Gray at low brightness
```

**When sent:**
- When user changes color in the color picker
- When user adjusts the brightness slider (debounced 50ms)

---

### 2. Temperature Display

#### ESP32 → App: Send Temperature
```
TEMP:temperature
```

**Parameters:**
- `temperature`: Temperature value in degrees (0-99)

**Examples:**
```
TEMP:25      # 25 degrees
TEMP:18      # 18 degrees
TEMP:0       # 0 degrees
```

**When sent:**
- On WebSocket connection established (welcome message)
- When temperature changes on ESP32

**Note:** The app displays the temperature with a degree symbol (°) on the Temperature screen.

---

### 3. Timer Control

#### App → ESP32: Start Timer
```
TIMER:START:seconds
```

**Parameters:**
- `seconds`: Total duration in seconds

**Examples:**
```
TIMER:START:300      # Start 5-minute timer (300 seconds)
TIMER:START:60       # Start 1-minute timer
TIMER:START:3600     # Start 1-hour timer
```

#### App → ESP32: Stop Timer
```
TIMER:STOP
```

**Parameters:** None

**Description:** Stops/pauses the currently running timer.

#### App → ESP32: Reset Timer
```
TIMER:RESET
```

**Parameters:** None

**Description:** Resets the timer to 00:00:00 and stops it.

---

### 4. Matrix Control (Custom LED Patterns)

#### App → ESP32: Set Individual LED Color
```
MATRIX_SET:ledIndex,r,g,b,brightness
```

**Parameters:**
- `ledIndex`: LED position in the physical array (0-120)
- `r`: Red component (0-255)
- `g`: Green component (0-255)
- `b`: Blue component (0-255)
- `brightness`: Brightness for this LED (0-255)

**Examples:**
```
MATRIX_SET:110,255,0,0,200       # Set LED 110 (letter 'E') to red
MATRIX_SET:60,0,255,255,255      # Set LED 60 (letter 'A') to cyan at full brightness
MATRIX_SET:10,128,0,128,150      # Set LED 10 (letter 'R') to purple
```

**LED Array Layout:**
The LEDs are arranged in a zigzag pattern (see led_setup.md for complete mapping):
```
Row  0: 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120  (ESKISTAFÜNF) →
Row  1: 109, 108, 107, 106, 105, 104, 103, 102, 101, 100,  99  (ZEHNJEPSORM) ←
Row  2:  88,  89,  90,  91,  92,  93,  94,  95,  96,  97,  98  (AFAXZWANZIG) →
Row  3:  87,  86,  85,  84,  83,  82,  81,  80,  79,  78,  77  (DREIVIERTEL) ←
Row  4:  66,  67,  68,  69,  70,  71,  72,  73,  74,  75,  76  (VORFUNKNACH) →
Row  5:  65,  64,  63,  62,  61,  60,  59,  58,  57,  56,  55  (HALBAELFÜNF) ←
Row  6:  44,  45,  46,  47,  48,  49,  50,  51,  52,  53,  54  (EINSXAMZWEI) →
Row  7:  43,  42,  41,  40,  39,  38,  37,  36,  35,  34,  33  (DREIPMJVIER) ←
Row  8:  22,  23,  24,  25,  26,  27,  28,  29,  30,  31,  32  (SECHSNLACHT) →
Row  9:  21,  20,  19,  18,  17,  16,  15,  14,  13,  12,  11  (SIEBENZWÖLF) ←
Row 10:   0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10  (ZEHNEUNKUHR) →
```
Note: Even rows go left→right, odd rows go right→left (zigzag pattern)

#### App → ESP32: Clear Individual LED
```
MATRIX_CLEAR:ledIndex
```

**Parameters:**
- `ledIndex`: LED position in the physical array (0-120)

**Examples:**
```
MATRIX_CLEAR:110     # Clear LED 110 (letter 'E')
MATRIX_CLEAR:60      # Clear LED 60 (letter 'A')
```

**When sent:**
- When user taps a letter in the Matrix screen to toggle it on/off
- App automatically converts row/column to LED index using the zigzag pattern (MatrixScreen.kt:34-46)
- Immediate transmission (no debouncing)

#### App → ESP32: Clear All LEDs
```
MATRIX_CLEAR_ALL
```

**Parameters:** None

**Description:** Clears all 121 LEDs (indices 0-120) in the matrix, setting them to off/default state.

**Examples:**
```
MATRIX_CLEAR_ALL     # Clear entire matrix
```

**When sent:**
- When user presses "Clear All" button on Matrix screen (MatrixScreen.kt:80-88)
- Immediate transmission (no debouncing)
- Provides bulk clear operation instead of 121 individual LED clears

**ESP32 Implementation Note:**
This is equivalent to sending 121 individual `MATRIX_CLEAR:N` commands but much more efficient. ESP32 should iterate through all LEDs 0-120 and turn them off.

---

## 5. Settings Synchronization

### App → ESP32: Request Settings
```
GET_SETTINGS
```
**Description:**
- Automatically sent by the app after successful WebSocket connection
- Requests the ESP32 to send all current settings (mode, color, brightness) to the app

**When sent:**
- Immediately after WebSocket connection is established (line 70 in WebSocketManager.kt)

### ESP32 → App: Send Current Settings
```
SETTINGS:mode,r,g,b,brightness
```
**Parameters:**
- `mode`: Current display mode (CLOCK, DIGITAL, MATRIX, TEMPERATURE)
- `r`: Red component (0-255)
- `g`: Green component (0-255)
- `b`: Blue component (0-255)
- `brightness`: Brightness level (0-255)

**Examples:**
```
SETTINGS:CLOCK,255,0,0,200         # Clock mode, red color, brightness 200
SETTINGS:TEMPERATURE,0,255,255,255 # Temperature mode, cyan, full brightness
SETTINGS:MATRIX,128,0,128,150      # Matrix mode, purple, brightness 150
```

**When sent:**
- As a response to `GET_SETTINGS` (immediately after connection)
- Optionally: When settings are changed on the ESP32

**Note:**
- The app updates its UI with the received values
- Settings are stored in a StateFlow for reactive updates (WebSocketManager.kt:37-38)

---

## 6. Mode Control

### App → ESP32: Set Display Mode
```
MODE:mode
```
**Parameters:**
- `mode`: Display mode (CLOCK, DIGITAL, MATRIX, TEMPERATURE, ALARM)

**Examples:**
```
MODE:CLOCK          # Switch to clock display
MODE:TEMPERATURE    # Switch to temperature display (also accepts MODE:TEMP)
MODE:MATRIX         # Switch to custom matrix mode
MODE:DIGITAL        # Switch to digital clock display
MODE:ALARM          # Switch to alarm/timer display
```

**When sent:**
- Automatically when user navigates between screens using bottom navigation (MainScreen.kt:104-116)
- When app starts up and restores last mode from SharedPreferences (MainScreen.kt:83-101)
- When MODE command received via widget (ModeWidgetProvider.kt:64-68)
- When SETTINGS message is received from ESP32 and app auto-navigates to matching screen (MainScreen.kt:118-144)
- Immediate transmission (no debouncing)

**Note:** The app widget also sends MODE: messages independently when tapped, even when the main app is closed (WidgetWebSocketClient.kt)

---

## 7. Schedule Control

### App → ESP32: Enable Schedule
```
SCHEDULE:ON,HH:MM,HH:MM
```
**Parameters:**
- First `HH:MM`: Start time (hours and minutes)
- Second `HH:MM`: End time (hours and minutes)

**Examples:**
```
SCHEDULE:ON,7:00,22:00     # Active from 7:00 AM to 10:00 PM
SCHEDULE:ON,6:30,23:45     # Active from 6:30 AM to 11:45 PM
SCHEDULE:ON,0:00,23:59     # Active all day
```

**When sent:**
- When user enables the schedule toggle (SettingsScreen.kt:196)
- When user changes start or end time (SettingsScreen.kt:224, 260)

### App → ESP32: Disable Schedule
```
SCHEDULE:OFF
```
**Parameters:** None

**Description:** Disables the automatic on/off schedule, clock stays on continuously

**When sent:**
- When user disables the schedule toggle (SettingsScreen.kt:198)

---

## Screen-Specific Behavior

### Clock Screen
- **Sends:** Nothing (displays current time using matrix, no communication)
- **Receives:** Nothing specific
- **Note:** Time calculation and display is handled entirely by the app

### Digital Clock Screen
- **Sends:** Nothing (displays current minutes, no communication)
- **Receives:** Nothing specific
- **Note:** Minute display is handled entirely by the app

### Temperature Screen
- **Sends:** Color and brightness updates
- **Receives:** `TEMP:xx` messages to display temperature

### Alarm/Timer Screen
- **Sends:** `TIMER:START:xxx`, `TIMER:STOP`, `TIMER:RESET`
- **Receives:** Nothing specific
- **Note:** The app displays a visual countdown timer in the UI (AlarmScreen.kt:52-61). ESP32 can independently handle its own countdown or rely on the app's TIMER commands. When user presses Start, the app both sends the WebSocket message and begins displaying a countdown that decrements every second until reaching zero.

### Matrix Screen
- **Sends:**
  - `MATRIX_SET:ledIndex,r,g,b,brightness` - Set individual LED
  - `MATRIX_CLEAR:ledIndex` - Clear individual LED
  - `MATRIX_CLEAR_ALL` - Clear all LEDs at once
- **Receives:** Nothing specific
- **Note:** Allows custom LED patterns with individual LED colors. Color selection in the color picker does NOT send WebSocket messages immediately - messages are only sent when the user taps a letter in the matrix grid (MatrixScreen.kt:101-122). The app automatically converts row/column coordinates to LED array indices (0-120) using the zigzag pattern defined in led_setup.md (MatrixScreen.kt:35-46). This allows users to preview and adjust colors locally before applying them. The "Clear All" button (MatrixScreen.kt:81-88) provides quick clearing of all 121 LEDs with a single command.

---

## Connection Lifecycle

### 1. App Startup
```
1. App connects to ESP32 WebSocket
2. Connection status shown in top-right indicator:
   - Gray: Disconnected
   - Amber: Connecting...
   - Green: Connected
   - Red: Error
```

### 2. Initial Handshake
After connection is established:
```
App → ESP32: "GET_SETTINGS"
ESP32 → App: "SETTINGS:CLOCK,255,0,0,200"
```
The app requests current settings, and ESP32 responds with current mode, color, and brightness.

Optionally, ESP32 may also send initial temperature:
```
ESP32 → App: "TEMP:25"
```

### 3. Ongoing Communication
- Color/brightness changes: Sent immediately (brightness debounced 50ms)
- Timer commands: Sent immediately on button press
- Matrix updates: Sent immediately on letter tap

### 4. Disconnection
- App automatically reconnects when entering foreground (ON_START)
- App disconnects when entering background (ON_STOP)

---

## Error Handling

### Invalid Messages
- ESP32 should ignore malformed messages
- No acknowledgment is expected for valid messages

### Connection Loss
- App shows status as "Disconnected" or "Error"
- App attempts to reconnect when returning to foreground
- ESP32 should handle disconnection gracefully (maintain state)

---

## Implementation Notes

### For ESP32 Developers:

1. **Parse incoming messages** by splitting on `:` and `,`
2. **Respond to GET_SETTINGS** immediately after connection with current state
3. **Update LEDs** based on received color/brightness values
4. **Handle timer** countdown logic on ESP32 side
5. **Send temperature** updates periodically or on change
6. **Store matrix state** to persist custom patterns
7. **Implement mode switching** (CLOCK, DIGITAL, MATRIX, TEMPERATURE)
8. **Handle schedule** logic to turn display on/off at specified times

### For App Developers:

1. **Debounce brightness** slider (50ms) to avoid message spam (CommonControls.kt:54)
2. **No debouncing** for discrete actions (timer buttons, matrix taps)
3. **Connection status** is tracked via WebSocketManager StateFlow (WebSocketManager.kt:33-34)
4. **Settings sync** happens automatically on connect via GET_SETTINGS (WebSocketManager.kt:70)
5. **Message replay** is disabled (replay=0) in current implementation (WebSocketManager.kt:30)

---

## Example Communication Flows

### Initial Connection and Setup
```
1. App connects to ws://192.168.3.210:81/ws
2. App → ESP32: "GET_SETTINGS"
3. ESP32 → App: "SETTINGS:CLOCK,255,0,0,200"
4. App updates UI with clock mode, red color, brightness 200
5. ESP32 → App: "TEMP:22" (optional welcome message)
```

### Switching Modes and Configuring Schedule
```
App → ESP32: "MODE:TEMPERATURE"            # Switch to temperature display
App → ESP32: "SCHEDULE:ON,7:00,22:00"      # Enable schedule 7 AM - 10 PM
App → ESP32: "COLOR:0,255,255,200"         # Set cyan color
ESP32: (Updates display mode, schedule, and color)
```

### Setting Up a Timer
```
App → ESP32: "MODE:CLOCK"                  # Switch back to clock mode
App → ESP32: "COLOR:255,100,0,200"         # Set orange color
App → ESP32: "TIMER:START:600"             # Start 10-minute timer
ESP32: (Starts countdown from 10:00)
App: (Displays visual countdown: 9:59, 9:58, 9:57...)
...
App → ESP32: "TIMER:STOP"                  # User stops timer
ESP32: (Pauses countdown)
App: (Pauses visual countdown)
App → ESP32: "TIMER:RESET"                 # User resets timer
ESP32: (Clears timer back to 00:00)
App: (Resets display to 00:00:00)
```

### Creating a Custom Pattern
```
App → ESP32: "MODE:MATRIX"                         # Switch to matrix mode
# User selects red color in color picker (no message sent yet)
App → ESP32: "MATRIX_SET:110,255,0,0,255"          # User taps 'E' (LED 110) - now message sent
App → ESP32: "MATRIX_SET:111,255,0,0,255"          # User taps 'S' (LED 111)
App → ESP32: "MATRIX_SET:113,255,0,0,255"          # User taps 'I' (LED 113)
ESP32: (Displays "ES I" in red)
App → ESP32: "MATRIX_CLEAR:111"                    # User taps 'S' again to clear
ESP32: (Displays "E  I" with S cleared)
```

### Clearing Matrix Pattern
```
App → ESP32: "MODE:MATRIX"                 # Switch to matrix mode
# User creates some pattern...
App → ESP32: "MATRIX_SET:110,255,0,0,255"  # Set several LEDs
App → ESP32: "MATRIX_SET:111,255,0,0,255"
App → ESP32: "MATRIX_SET:113,255,0,0,255"
ESP32: (Displays pattern)
# User presses "Clear All" button
App → ESP32: "MATRIX_CLEAR_ALL"            # Single command clears everything
ESP32: (All LEDs turn off)
App: (Local display clears)
```

### Temperature Display Update
```
App → ESP32: "MODE:TEMPERATURE"            # Switch to temperature mode
ESP32 → App: "TEMP:25"                     # Send current temperature
App: (Updates temperature display to "25°")
[Some time later...]
ESP32 → App: "TEMP:26"                     # Temperature increased
App: (Updates temperature display to "26°")
```

### Widget Mode Switching (App Closed)
```
# User taps widget on home screen while app is closed
Widget → ESP32: "MODE:DIGITAL"             # Widget connects, sends mode, disconnects
ESP32: (Switches to digital clock mode)
# Widget works independently using stored IP/Port from SharedPreferences
```

### Disabling Schedule
```
App → ESP32: "SCHEDULE:OFF"                # Disable time-based control
ESP32: (Clock stays on continuously, ignoring time schedule)
```

---

## Version History

- **v1.3** (2025-12-11): Matrix API changed to use LED array indices
  - **BREAKING CHANGE**: Matrix commands now use LED index (0-120) instead of row,col coordinates
  - Changed `MATRIX_SET:row,col,r,g,b,brightness` to `MATRIX_SET:ledIndex,r,g,b,brightness`
  - Changed `MATRIX_CLEAR:row,col` to `MATRIX_CLEAR:ledIndex`
  - Added `MATRIX_CLEAR_ALL` command for bulk clearing all LEDs
  - Added "Clear All" button to Matrix screen UI (MatrixScreen.kt:81-88)
  - Added complete LED array layout documentation showing zigzag pattern (led_setup.md)
  - App automatically converts row/column to LED index using zigzag algorithm (MatrixScreen.kt:35-46)
  - Updated all matrix examples to use LED indices
  - This change aligns the API with the physical LED wiring and simplifies ESP32 implementation

- **v1.2** (2025-12-11): Updated for recent app enhancements
  - Changed SET_MODE to MODE format (MODE:CLOCK, MODE:DIGITAL, etc.)
  - Added ALARM mode to mode control
  - Documented automatic mode switching on navigation (MainScreen.kt)
  - Added widget mode switching behavior and independent operation
  - Clarified timer countdown: app displays visual countdown while ESP32 handles physical countdown
  - Documented Matrix screen color selection behavior: no immediate WebSocket sends, only on letter tap
  - Added widget example showing independent operation when app is closed
  - Updated all example communication flows to use MODE: instead of SET_MODE:
  - Added code references for new features

- **v1.1** (2025-12-10): Updated API documentation to match current implementation
  - Fixed SETTINGS message format (was incorrect, now: mode,r,g,b,brightness)
  - Added GET_SETTINGS command
  - Added SET_MODE command for switching display modes
  - Added SCHEDULE commands (ON/OFF) for time-based control
  - Added code references to implementation
  - Improved connection lifecycle documentation

- **v1.0** (2025-01-07): Initial API documentation
  - COLOR command
  - TEMP command
  - TIMER commands
  - MATRIX commands
