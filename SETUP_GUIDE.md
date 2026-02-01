# 📘 EchoState - Guia Completo de Setup e Utilização

## 📋 Índice

1. [Visão Geral do Projeto](#visão-geral-do-projeto)
2. [Arquitetura Completa](#arquitetura-completa)
3. [Tecnologias Utilizadas](#tecnologias-utilizadas)
4. [Setup Inicial](#setup-inicial)
5. [Configuração do Grafana](#configuração-do-grafana)
6. [Interpretação dos Gráficos](#interpretação-dos-gráficos)
7. [Troubleshooting](#troubleshooting)
8. [Próximos Passos](#próximos-passos)

---

## 🎯 Visão Geral do Projeto

O **EchoState** é uma plataforma de monitorização distribuída que simula um sistema de observabilidade real usado em ambientes de produção. O objetivo é monitorizar a **latência de rede** e **disponibilidade de serviços** em tempo real.

### Como Funciona?

1. **Agente Python** → Envia pedidos HTTP ao serviço alvo (google.com) a cada 5 segundos
2. **Mede a latência** → Calcula quanto tempo demora a resposta
3. **Envia para Backend Java** → Transmite os dados via HTTP POST (JSON)
4. **Backend processa** → Valida e grava os dados no InfluxDB
5. **Grafana visualiza** → Apresenta gráficos em tempo real

---

## 🏗️ Arquitetura Completa

```
┌─────────────────────┐
│   Python Agent      │
│   (Container 1)     │
│                     │
│  • Faz ping HTTP    │
│  • Mede latência    │
│  • Envia JSON       │
└──────────┬──────────┘
           │ HTTP POST
           │ JSON Payload
           ▼
┌─────────────────────┐
│  Spring Boot API    │
│  (Container 2)      │
│                     │
│  • Recebe métricas  │
│  • Valida dados     │
│  • Grava InfluxDB   │
└──────────┬──────────┘
           │ Write API
           │ Line Protocol
           ▼
┌─────────────────────┐
│     InfluxDB        │
│  (Container 3)      │
│                     │
│  • Base de dados    │
│    time-series      │
│  • Bucket: metrics  │
└──────────┬──────────┘
           │ Flux Query
           │ Data Source
           ▼
┌─────────────────────┐
│      Grafana        │
│  (Container 4)      │
│                     │
│  • Dashboards       │
│  • Gráficos RT      │
│  • Alertas          │
└─────────────────────┘
```

### Fluxo de Dados Detalhado

**1. Agente coleta dados:**
```python
# O agente faz um pedido HTTP
response = requests.get("http://google.com", timeout=5)
latency = (tempo_fim - tempo_inicio) * 1000  # Converte para milissegundos
```

**2. Envia JSON para o Backend:**
```json
{
  "host_name": "a3eb4e8f7c5e",
  "target_service": "google.com",
  "latency_ms": 325,
  "status": "UP",
  "timestamp": "2026-02-01T16:42:22"
}
```

**3. Backend transforma em InfluxDB Line Protocol:**
```
network_latency,host=a3eb4e8f7c5e,status=UP,target=google.com latency_ms=325i 1769964142719
```

**4. Grafana consulta com Flux:**
```flux
from(bucket: "metrics_bucket")
  |> range(start: -15m)
  |> filter(fn: (r) => r["_measurement"] == "network_latency")
  |> filter(fn: (r) => r["_field"] == "latency_ms")
```

---

## 💻 Tecnologias Utilizadas

### Backend (Java)
- **Java 17** - Linguagem de programação
- **Spring Boot 3.5.10** - Framework para APIs REST
- **InfluxDB Java Client** - Cliente para comunicar com InfluxDB
- **Lombok** - Reduz código boilerplate (getters/setters)
- **Maven** - Gestão de dependências

### Agent (Python)
- **Python 3.10** - Linguagem de programação
- **requests** - Biblioteca HTTP para fazer pedidos
- **platform** - Obter informação do sistema (hostname)
- **datetime** - Timestamps precisos

### Infrastructure
- **Docker** - Containerização de serviços
- **Docker Compose** - Orquestração de múltiplos containers
- **InfluxDB 2.7** - Base de dados time-series
- **Grafana** - Plataforma de visualização e dashboards

---

## 🚀 Setup Inicial

### Pré-requisitos

```bash
# Verificar instalações
docker --version          # Docker 20.10+
docker compose version    # Docker Compose 2.0+
java -version            # Java 17+
python --version         # Python 3.10+
```

### Passo 1: Clonar/Preparar o Projeto

```bash
cd C:\Projects\Echostate

# Estrutura do projeto:
# ├── docker-compose.yml
# ├── backend/
# │   ├── Dockerfile
# │   ├── pom.xml
# │   └── src/
# └── agent/
#     ├── Dockerfile
#     ├── main.py
#     └── requirements.txt
```

### Passo 2: Compilar o Backend

```bash
cd backend
./mvnw clean package -DskipTests
cd ..
```

**O que isto faz:**
- `clean` - Remove builds anteriores
- `package` - Compila o código Java e cria um `.jar`
- `-DskipTests` - Salta os testes (para ser mais rápido)

**Output esperado:**
```
BUILD SUCCESS
Total time: 6.652 s
```

### Passo 3: Iniciar Todos os Serviços

```bash
docker compose up --build -d
```

**O que isto faz:**
- `up` - Inicia os containers
- `--build` - Reconstrói as imagens Docker
- `-d` - Detached mode (corre em background)

**Output esperado:**
```
[+] up 7/7
 ✔ Network echostate_monitoring-net Created
 ✔ Container echostate-influxdb-1   Created
 ✔ Container echostate-backend-1    Created
 ✔ Container echostate-agent-1      Created
 ✔ Container echostate-grafana-1    Created
```

### Passo 4: Verificar que Está a Funcionar

```bash
# Verificar logs do agente
docker compose logs agent --tail=10

# Deve ver:
# ✅ Sucesso! O Java respondeu: Dados guardados no InfluxDB!

# Verificar logs do backend
docker compose logs backend --tail=10

# Deve ver:
# ✅ Gravado no InfluxDB com sucesso!
```

---

## 📊 Configuração do Grafana

### Acesso Inicial

1. Abrir navegador: **http://localhost:3000**
2. Credenciais:
   - **Username:** `admin`
   - **Password:** `admin`
3. (Opcional) Mudar password quando pedido

---

### Configurar Data Source (InfluxDB)

#### 1️⃣ Aceder às Configurações

- Clicar no ícone **⚙️** (Settings) na barra lateral esquerda
- Selecionar **Data Sources**
- Clicar no botão **Add data source**

#### 2️⃣ Selecionar InfluxDB

- Procurar e clicar em **InfluxDB**

#### 3️⃣ Configuração Detalhada

| Campo | Valor | Explicação |
|-------|-------|------------|
| **Name** | `influxdbEchoState` | Nome identificador do data source |
| **Query Language** | `Flux` | Linguagem moderna do InfluxDB 2.x |
| **URL** | `http://influxdb:8086` | Endereço interno Docker |
| **Organization** | `echostate` | Organização definida no docker-compose |
| **Token** | `my-super-secret-auth-token` | Token de autenticação |
| **Default Bucket** | `metrics_bucket` | Bucket onde os dados são guardados |

#### 4️⃣ Testar Conexão

- Clicar em **Save & Test** no final da página
- Deve aparecer: ✅ **"datasource is working. 3 buckets found"**

---

### Criar Dashboard

#### 1️⃣ Criar Novo Dashboard

- Clicar em **+ (Create)** na barra lateral
- Selecionar **Dashboard**
- Clicar em **Add visualization**
- Selecionar o data source **influxdbEchoState**

#### 2️⃣ Configurar Painel de Latência

**Query (Flux):**
```flux
from(bucket: "metrics_bucket")
  |> range(start: -15m)
  |> filter(fn: (r) => r["_measurement"] == "network_latency")
  |> filter(fn: (r) => r["_field"] == "latency_ms")
```

**Explicação da Query:**
- `from(bucket: "metrics_bucket")` - Origem dos dados
- `range(start: -15m)` - Últimos 15 minutos
- `filter(_measurement)` - Filtra a medição "network_latency"
- `filter(_field)` - Filtra o campo numérico "latency_ms"

**Configurações do Painel:**

| Opção | Valor |
|-------|-------|
| **Panel title** | `Latência de Rede - google.com` |
| **Visualization** | Time series |
| **Unit** | milliseconds (ms) |
| **Legend** | Show |
| **Tooltip mode** | All |

**Opções Avançadas:**
- **Min:** 0
- **Max:** Auto
- **Decimals:** 0
- **Color scheme:** Green-Yellow-Red (by value)

#### 3️⃣ Adicionar Painel de Status (Opcional)

**Query:**
```flux
from(bucket: "metrics_bucket")
  |> range(start: -5m)
  |> filter(fn: (r) => r["_measurement"] == "network_latency")
  |> filter(fn: (r) => r["_field"] == "latency_ms")
  |> last()
```

**Configurações:**
- **Visualization:** Stat
- **Title:** `Status Atual`
- **Color mode:** Background
- **Thresholds:**
  - Verde: 0 - 100ms (Excelente)
  - Amarelo: 100 - 300ms (Aceitável)
  - Laranja: 300 - 500ms (Lento)
  - Vermelho: 500ms+ (Crítico)

#### 4️⃣ Adicionar Painel de Disponibilidade

**Query:**
```flux
from(bucket: "metrics_bucket")
  |> range(start: -1h)
  |> filter(fn: (r) => r["_measurement"] == "network_latency")
  |> filter(fn: (r) => r["status"] == "UP")
  |> count()
  |> yield(name: "uptime_count")
```

**Configurações:**
- **Visualization:** Stat
- **Title:** `Checks Bem-Sucedidos (1h)`
- **Unit:** none

#### 5️⃣ Guardar Dashboard

- Clicar em **💾 Save dashboard** (canto superior direito)
- Nome: `EchoState - Network Monitoring`
- Clicar em **Save**

---

## 📈 Interpretação dos Gráficos

### Gráfico de Latência (Time Series)

```
 Latência (ms)
 ↑
700│                    •
600│         •                •
500│    •         •
400│ •                            •
300│                                  •
200│                                      •
 ↓ ─────────────────────────────────────→
   16:42  16:43  16:44  16:45  16:46  Tempo
```

#### O que Significa Cada Linha?

- **Eixo Y (Vertical):** Latência em milissegundos (ms)
- **Eixo X (Horizontal):** Tempo (timestamps)
- **Pontos/Linha:** Cada medição do agente (a cada 5 segundos)

#### Interpretação dos Valores

| Latência | Classificação | Significado |
|----------|---------------|-------------|
| **0-50ms** | 🟢 Excelente | Ligação muito rápida (local/CDN) |
| **50-150ms** | 🟢 Muito Bom | Normal para sites internacionais |
| **150-300ms** | 🟡 Aceitável | Ligação OK, possível saturação |
| **300-500ms** | 🟠 Lenta | Pode indicar problemas de rede |
| **500ms+** | 🔴 Crítica | Problemas graves ou timeout |
| **-1ms** | 🔴 ERRO | Serviço inacessível (DOWN) |

#### Padrões Comuns

**1. Linha Estável (~300-400ms)**
```
400ms ─────────────────
      Normal para google.com em Portugal
```
- **Significa:** Rede estável, latência consistente
- **Ação:** Nenhuma, está normal

**2. Picos Ocasionais**
```
ms
600│     •
400│─────────────────
200│
```
- **Significa:** Congestão momentânea ou packet loss
- **Ação:** Monitorizar se persistir

**3. Tendência Crescente**
```
ms
600│                 •••
400│         •••
200│ •••
```
- **Significa:** Degradação progressiva da rede
- **Ação:** Investigar firewall, router ou ISP

**4. Quedas a 0 ou -1**
```
ms
400│─────    ─────────
  0│     ────
```
- **Significa:** Serviço ficou inacessível
- **Ação:** Verificar conectividade ou DNS

---

### Painel de Status (Stat)

```
┌─────────────────┐
│  🟢 325ms       │
│  Status: UP     │
└─────────────────┘
```

**Cores:**
- 🟢 **Verde:** Serviço UP, latência boa
- 🟡 **Amarelo:** UP mas lento
- 🔴 **Vermelho:** DOWN ou latência crítica

---

### Métricas Adicionais (Opcional)

#### Latência Média (1h)
```flux
from(bucket: "metrics_bucket")
  |> range(start: -1h)
  |> filter(fn: (r) => r._measurement == "network_latency")
  |> filter(fn: (r) => r._field == "latency_ms")
  |> mean()
```

#### Latência Máxima
```flux
from(bucket: "metrics_bucket")
  |> range(start: -1h)
  |> filter(fn: (r) => r._measurement == "network_latency")
  |> filter(fn: (r) => r._field == "latency_ms")
  |> max()
```

---

## 🔧 Troubleshooting

### Problema: "No data" no Grafana

**Diagnóstico:**
```bash
# 1. Verificar se o agente está a enviar
docker compose logs agent --tail=5

# Deve ver: ✅ Sucesso!
```

**Soluções:**
1. **Mudar intervalo de tempo** no Grafana:
   - Clicar no relógio (canto superior direito)
   - Selecionar "Last 5 minutes"

2. **Verificar dados no InfluxDB:**
```bash
docker compose exec influxdb influx query \
  'from(bucket:"metrics_bucket") |> range(start:-10m)' \
  --org echostate --token my-super-secret-auth-token
```

3. **Reiniciar agente:**
```bash
docker compose restart agent
```

---

### Problema: Backend não grava dados

**Sintomas:**
```
The point doesn't contains any fields, skipping
```

**Solução:**
```bash
# Recompilar backend
cd backend
./mvnw clean package -DskipTests
cd ..
docker compose up --build -d backend
```

---

### Problema: Agente com KeyError

**Sintomas:**
```python
KeyError: 'hostName'
```

**Solução:**
- Verificar que o `main.py` usa `host_name` (snake_case)
- Verificar que o DTO Java tem `@JsonProperty("host_name")`

---

### Problema: InfluxDB não está a aceitar token

**Sintomas:**
```
unauthorized: unauthorized access
```

**Solução:**
```bash
# Recriar volumes
docker compose down -v
docker compose up --build -d

# Aguardar 30 segundos para o InfluxDB inicializar
```

---

### Comandos Úteis

```bash
# Ver todos os containers ativos
docker compose ps

# Ver logs de todos os serviços
docker compose logs -f

# Ver logs de um serviço específico
docker compose logs backend -f

# Parar tudo
docker compose down

# Parar e remover volumes (reset completo)
docker compose down -v

# Reiniciar um serviço específico
docker compose restart agent

# Entrar num container (debug)
docker compose exec backend bash
docker compose exec influxdb bash
```

---

## 🎓 Conceitos Aprendidos

### 1. **Arquitetura Distribuída**
- Comunicação entre serviços via HTTP
- Containers isolados mas conectados
- Push vs Pull monitoring

### 2. **Time-Series Databases**
- InfluxDB otimizado para dados temporais
- Line Protocol (formato eficiente)
- Queries com Flux (linguagem funcional)

### 3. **Observabilidade**
- **Métricas:** Números que mudam ao longo do tempo
- **Logs:** Eventos que aconteceram
- **Traces:** Caminho de uma request (não implementado)

### 4. **Spring Boot (Java)**
- REST Controllers (`@RestController`)
- Dependency Injection (`@Service`)
- Configuration Management (`application.properties`)

### 5. **Docker & Networking**
- `networks` para comunicação inter-container
- DNS interno Docker (nome do serviço = hostname)
- Volumes para persistência

---

## 🚀 Próximos Passos

### Melhorias Imediatas

1. **Adicionar mais targets:**
```python
targets = ["google.com", "github.com", "stackoverflow.com"]
for target in targets:
    check_and_send(target)
```

2. **Alertas no Grafana:**
   - Configurar alerta se latência > 500ms por 2 minutos
   - Enviar email ou Slack notification

3. **Autenticação no Backend:**
```java
@PreAuthorize("hasRole('AGENT')")
public ResponseEntity<String> receiveMetric(...)
```

### Expansões Futuras

- [ ] Deploy em Cloud (AWS/Azure/GCP)
- [ ] Múltiplos agentes em diferentes regiões
- [ ] Dashboard público (read-only)
- [ ] Histórico de incidentes
- [ ] Machine Learning para deteção de anomalias

---

## 📚 Recursos Adicionais

- [InfluxDB Flux Documentation](https://docs.influxdata.com/flux/)
- [Grafana Dashboard Examples](https://grafana.com/grafana/dashboards/)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Docker Compose Networking](https://docs.docker.com/compose/networking/)

---

## 👨‍💻 Autor

**João Daniel Evaristo**
- ISEL - Network & Computer Engineering
- Focus: Backend, DevOps, Networks

---

## 📝 Notas Finais

Este projeto demonstra conceitos fundamentais de:
- **Engenharia de Redes:** Latência, disponibilidade, monitorização
- **DevOps:** Containerização, CI/CD readiness
- **Backend Development:** APIs REST, persistência, validação
- **Data Visualization:** Dashboards, time-series, alerting

Pode ser usado como base para um sistema de produção real com as devidas melhorias de segurança, escalabilidade e resiliência.
