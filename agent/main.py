import time
import requests
import platform
from datetime import datetime

# --- CONFIGURAÇÕES ---
# O endereço onde o teu servidor Java está à escuta
SERVER_URL = "http://localhost:8080/api/metrics"
# O site que vamos "vigiar"
TARGET_HOST = "google.com"


def get_system_metrics():
    """
    Recolhe dados do sistema e monta o JSON (o DTO).
    """
    start_time = time.time()

    # 1. Simula um "Ping" (Check HTTP)
    try:
        response = requests.get(f"http://{TARGET_HOST}", timeout=5)
        latency = int((time.time() - start_time) * 1000)  # Converte para ms
        status = "UP" if response.status_code == 200 else "DOWN"
    except:
        latency = 0
        status = "DOWN"

    # 2. Monta o Pacote de Dados (Payload)
    # IMPORTANTE: Os nomes das chaves (hostName, ipAddress...) TÊM de ser iguais ao Java!
    payload = {
        "hostName": platform.node(),
        "ipAddress": "127.0.0.1",  # Hardcoded por enquanto
        "targetService": TARGET_HOST,
        "latencyMs": latency,
        "status": status,
        "timestamp": datetime.now().isoformat()
    }

    return payload


def send_data():
    """
    Envia os dados para o servidor Java via POST.
    """
    data = get_system_metrics()
    print(f"📤 A enviar dados de {data['hostName']}...")

    try:
        # O momento da verdade: O POST Request
        response = requests.post(SERVER_URL, json=data)

        if response.status_code == 200:
            print(f"✅ Sucesso! O Java respondeu: {response.text}")
        else:
            print(f"⚠️ Erro do Servidor: {response.status_code}")

    except Exception as e:
        print(f"❌ Falha na conexão. O servidor Java está ligado? \n   Erro: {e}")


# --- LOOP INFINITO ---
if __name__ == "__main__":
    print("🚀 Agente EchoState Iniciado...")

    # Loop para enviar dados a cada 5 segundos
    while True:
        send_data()
        time.sleep(5)