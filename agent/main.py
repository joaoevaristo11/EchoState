import time
import requests
import platform
from datetime import datetime
import os

SERVER_URL = os.getenv("SERVER_URL","http://localhost:8080/api/metrics") #url onde o java recebe os dados
TARGET_HOST = "google.com" #site que vamos estar a vigiar


def receive_data():
    start_time = time.time()
    ## 1. Simula um "Ping" (Check HTTP)
    try:
        response = requests.get(f"http://{TARGET_HOST}", timeout=5)
        latency = int((time.time() - start_time) * 1000) # Converte para ms

        if response.status_code == 200:
            status = "UP"
        else:
            status = "DOWN"

    except:
        latency = 0
        status = "DOWN"

    # 2. Monta o Pacote de Dados (Payload)
    payload = {
        "hostName": platform.node(),
        "ipAddress": "127.0.0.1", # Hardcoded por enquanto
        "targetService": TARGET_HOST,
        "latencyMs": latency,
        "status": status,
        "timestamp": datetime.now().isoformat()
    }

    return payload


def send_data():
    data = receive_data()
    print(f"📤 A enviar dados de {data['hostName']}...")

    try:
        response = requests.post(SERVER_URL, json=data)
        if response.status_code == 200:
            print(f"✅ Sucesso! O Java respondeu: {response.text}")
        else:
            print(f"⚠️ Erro do Servidor: {response.status_code}")

    except Exception as e:
        print(f"❌ Falha na conexão. O servirdor Java está ligado?\n Erro: {e}")

if __name__ == "__main__":
    print("🚀 Agente EchoState Iniciado...")

    while True: # Loop para enviar dados a cada 5 segundos
        send_data()
        time.sleep(5)