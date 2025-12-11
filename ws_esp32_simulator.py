import asyncio
import websockets

# Konfiguration
HOST = '0.0.0.0'  # Lauscht auf allen Interfaces
PORT = 81
PATH = '/ws'

# Beispiel-Einstellungen, die simuliert werden
SCHEDULE_MSG = 'SCHEDULE:1,22,0,7,0'
SETTINGS_MSG='SETTINGS: DIGITAL, 255,0,255,200'  #Rot, Helligkeit 200'
TEMP_MSG = 'TEMP:9'

async def handler(websocket):
    print(f'Client verbunden: {websocket.remote_address}')
    # Sende initiale Nachrichten bei neuer Verbindung
    await websocket.send(TEMP_MSG)
    print(f'Sende: {TEMP_MSG}')
    await websocket.send(SETTINGS_MSG)
    print(f'Sende: {SETTINGS_MSG}')
    await websocket.send(SCHEDULE_MSG)
    print(f'Sende: {SCHEDULE_MSG}')
    try:
        async for message in websocket:
            print(f'Empfangen: {message}')
            if message == 'GET_SETTINGS':
                await websocket.send(SETTINGS_MSG)
                print(f'Sende: {SETTINGS_MSG}')
            if message == 'MODE:TEMP':
                await websocket.send(TEMP_MSG)
                print(f'Sende: {TEMP_MSG}')
            # Hier können weitere Kommandos simuliert werden
    except websockets.ConnectionClosed:
        print('Verbindung geschlossen')

async def main():
    import socket
    try:
        # Versuche die lokale IP zu ermitteln
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        local_ip = s.getsockname()[0]
        s.close()
    except Exception:
        local_ip = "localhost"
    
    print(f'Starte ESP32-WS-Simulator auf ws://{local_ip}:{PORT}{PATH}')
    print(f'(Lauscht auf allen Interfaces: {HOST})')
    async with websockets.serve(handler, HOST, PORT, process_request=None):
        await asyncio.Future()  # Läuft für immer

if __name__ == '__main__':
    asyncio.run(main())

