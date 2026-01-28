# 📡 EchoState - Distributed Network Health Monitor

> A lightweight, agent-based observability platform designed to monitor network latency and service availability in real-time.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-green)
![Python](https://img.shields.io/badge/Python-3.x-blue)
![Status](https://img.shields.io/badge/Status-Phase_1_(MVP)-yellow)

## 📖 Overview

**EchoState** is a distributed monitoring system that bridges the gap between **Network Engineering** and **DevOps**. It uses lightweight Python agents deployed at the edge (on different subnets or regions) to collect network metrics (latency, availability) and pushes them to a central Spring Boot backend.

### 🏗️ Architecture (Push Model)

The system follows a **Push-based architecture**, allowing agents to operate behind NATs/Firewalls without requiring inbound open ports.

```mermaid
graph LR
    A[📡 Python Agent] -- HTTP POST (JSON) --> B[☕ Spring Boot API]
    A2[📡 Python Agent 2] -- HTTP POST --> B
    subgraph Backend
    B --> C((Log/Processing))
    end
