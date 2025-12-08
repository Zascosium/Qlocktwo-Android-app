# ESP32 WebSocket API Documentation

## Overview
This document describes the WebSocket communication protocol between the Android app and the ESP32 QLOCKTWO device.

**Connection Details:**
- Protocol: WebSocket (ws://)
- Default Port: 81
- Path: `/ws`
- Example: `ws://192.168.3.210:81/ws`

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

#### App → ESP32: Set Individual Letter Color
```
MATRIX_SET:row,col,r,g,b,brightness
```

**Parameters:**
- `row`: Row index (0-10)
- `col`: Column index (0-10)
- `r`: Red component (0-255)
- `g`: Green component (0-255)
- `b`: Blue component (0-255)
- `brightness`: Brightness for this letter (0-255)

**Examples:**
```
MATRIX_SET:0,0,255,0,0,200       # Set top-left letter 'E' to red
MATRIX_SET:5,5,0,255,255,255     # Set letter 'A' to cyan at full brightness
MATRIX_SET:10,10,128,0,128,150   # Set bottom-right letter 'R' to purple
```

**Matrix Layout Reference:**
```
     0123456789A (columns)
   ┌───────────┐
 0 │ESKISTAFÜNF│
 1 │ZEHNJEPSORM│
 2 │AFAXZWANZIG│
 3 │DREIVIERTEL│
 4 │VORFUNKNACH│
 5 │HALBAELFÜNF│
 6 │EINSXAMZWEI│
 7 │DREIPMJVIER│
 8 │SECHSNLACHT│
 9 │SIEBENZWÖLF│
10 │ZEHNEUNKUHR│
   └───────────┘
(rows)
```

#### App → ESP32: Clear Individual Letter
```
MATRIX_CLEAR:row,col
```

**Parameters:**
- `row`: Row index (0-10)
- `col`: Column index (0-10)

**Examples:**
```
MATRIX_CLEAR:0,0     # Clear top-left letter 'E'
MATRIX_CLEAR:5,5     # Clear letter 'A'
```

**When sent:**
- When user taps a letter in the Matrix screen to toggle it on/off
- Immediate transmission (no debouncing)

---

## 5. Einstellungen abfragen und synchronisieren

### App → ESP32: Einstellungen anfordern
```
GET_SETTINGS
```
**Beschreibung:**
- Wird automatisch von der App nach erfolgreichem WebSocket-Connect gesendet.
- Fordert den ESP32 auf, alle aktuellen Einstellungen (z.B. Zeitplan, Start-/Endzeit, Farbe, Helligkeit) an die App zu senden.

### ESP32 → App: Aktuelle Einstellungen senden
```
SETTINGS:schedule_enabled,start_hour,start_minute,end_hour,end_minute,color_r,color_g,color_b,brightness
```
**Parameter:**
- `schedule_enabled`: 0 (aus) oder 1 (an)
- `start_hour`: Startstunde (0-23)
- `start_minute`: Startminute (0-59)
- `end_hour`: Endstunde (0-23)
- `end_minute`: Endminute (0-59)
- `color_r`: Rotwert (0-255)
- `color_g`: Grünwert (0-255)
- `color_b`: Blauwert (0-255)
- `brightness`: Helligkeit (0-255)

**Beispiel:**
```
SETTINGS:1,7,0,22,0,255,0,0,200
```
(Schedule aktiv, 7:00-22:00, Farbe Rot, Helligkeit 200)

**Wann gesendet:**
- Als Antwort auf `GET_SETTINGS` (direkt nach Verbindungsaufbau)
- Optional: Bei Änderungen der Einstellungen

**Hinweis:**
- Die App aktualisiert ihre UI mit den empfangenen Werten.

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
- **Note:** Timer countdown should be handled by ESP32

### Matrix Screen
- **Sends:** `MATRIX_SET:row,col,r,g,b,brightness` and `MATRIX_CLEAR:row,col`
- **Receives:** Nothing specific
- **Note:** Allows custom LED patterns with individual letter colors

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

### 2. Welcome Message (ESP32 → App)
```
TEMP:25
```
Sent immediately upon connection to provide initial temperature.

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
2. **Update LEDs** based on received color/brightness values
3. **Handle timer** countdown logic on ESP32 side
4. **Send temperature** updates periodically or on change
5. **Store matrix state** to persist custom patterns

### For App Developers:

1. **Debounce brightness** slider (50ms) to avoid message spam
2. **No debouncing** for discrete actions (timer buttons, matrix taps)
3. **Connection status** is tracked via WebSocketManager StateFlow
4. **Message replay** is enabled (replay=1) for late subscribers

---

## Example Communication Flow

### Setting Up a Timer
```
App → ESP32: "COLOR:255,100,0,200"     # Set orange color
App → ESP32: "TIMER:START:600"          # Start 10-minute timer
ESP32: (Starts countdown from 10:00)
...
App → ESP32: "TIMER:STOP"               # User stops timer
ESP32: (Pauses countdown)
App → ESP32: "TIMER:RESET"              # User resets timer
ESP32: (Clears timer back to 00:00)
```

### Creating a Custom Pattern
```
App → ESP32: "MATRIX_SET:0,0,255,0,0,255"      # Set 'E' to red
App → ESP32: "MATRIX_SET:0,1,255,0,0,255"      # Set 'S' to red
App → ESP32: "MATRIX_SET:0,3,255,0,0,255"      # Set 'I' to red
ESP32: (Displays "ES I" in red)
App → ESP32: "MATRIX_CLEAR:0,1"                 # Clear 'S'
ESP32: (Displays "E  I" with S cleared)
```

### Temperature Update
```
ESP32 → App: "TEMP:25"                  # Send current temperature
App: (Updates temperature display to "25°")
[Some time later...]
ESP32 → App: "TEMP:26"                  # Temperature increased
App: (Updates temperature display to "26°")
```

---

## Version History

- **v1.0** (2025-01-07): Initial API documentation
  - COLOR command
  - TEMP command
  - TIMER commands
  - MATRIX commands
