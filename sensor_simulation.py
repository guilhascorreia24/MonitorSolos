import json
import time
import random
from datetime import datetime
import paho.mqtt.client as mqtt

# Configurações do Broker
BROKER = "localhost"
PORT = 1883
TOPIC = "vineyard/sensors/simulated-01"
CLIENT_ID = "python-sensor-simulator"

def get_sensor_data(mode="NORMAL"):
    """Gera dados simulados do sensor baseados no modo"""
    
    # Valores base
    soil_moisture = round(random.uniform(40.0, 60.0), 2)
    air_temp = round(random.uniform(20.0, 25.0), 2)
    air_hum = round(random.uniform(40.0, 60.0), 2)

    if mode == "RISK_MEDIUM":
        air_hum = round(random.uniform(76.0, 82.0), 2)
        air_temp = round(random.uniform(16.0, 28.0), 2)
    elif mode == "RISK_HIGH" or mode == "CRITICAL":
        air_hum = round(random.uniform(86.0, 95.0), 2)
        air_temp = round(random.uniform(19.0, 24.0), 2)

    return {
        "deviceId": "sensor-simulated-001",
        "timestamp": datetime.utcnow().isoformat() + "Z", 
        "soilMoisture": soil_moisture,
        "airTemperature": air_temp,
        "airHumidity": air_hum
    }

def on_connect(client, userdata, flags, rc, properties=None):
    if rc == 0:
        print(f"✅ Conectado ao Broker MQTT em {BROKER}:{PORT}")
    else:
        print(f"❌ Falha na conexão. Código: {rc}")

def run():
    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, CLIENT_ID)
    client.on_connect = on_connect

    modes = ["NORMAL"] * 5 + ["RISK_MEDIUM"] * 10 + ["RISK_HIGH"] * 10 + ["CRITICAL"] * 10
    mode_idx = 0

    try:
        client.connect(BROKER, PORT, 60)
        client.loop_start()

        print("🚀 Iniciando simulação cíclica de estados...")
        while True:
            current_mode = modes[mode_idx % len(modes)]
            payload = get_sensor_data(current_mode)
            payload_json = json.dumps(payload)
            
            client.publish(TOPIC, payload_json)
            print(f"[{current_mode}] Enviado: {payload_json}")
            
            mode_idx += 1
            time.sleep(2) # Envia a cada 2 segundos para acelerar testes

    except KeyboardInterrupt:
        print("\nParando simulador...")
        client.loop_stop()

if __name__ == "__main__":
    run()