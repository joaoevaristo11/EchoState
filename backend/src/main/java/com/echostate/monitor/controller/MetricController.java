package com.echostate.monitor.controller;

import com.echostate.monitor.dto.NetworkMetricDTO;
import com.echostate.monitor.service.InfluxDBService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // Faz com que esta classe gere endpoints HTTP e responde com JSON em vez de HTML
@RequestMapping("/api/metrics") // Define o endereço base: http://localhost:8080/api/metrics,
                                // todo o tráfego encaminhado para esta rota deve ser tratado por esta classe

public class MetricController {

    private final InfluxDBService influxDBService;

    // Injeção de Dependência: O Spring traz-nos o Service automaticamente
    public MetricController(InfluxDBService influxDBService){
        this.influxDBService = influxDBService;
    }

    @PostMapping //utilizamos Post porque o Agente está a enviar dados novos ao servidor
    public ResponseEntity<String> receiveMetric(@RequestBody/*pega no body em JSON do pedido HTTP e transforma-o num objeto NetworkMetricDTO*/
                                NetworkMetricDTO metric)
    {
        influxDBService.writeMetric(metric); // 1. Gravar na Base de Dados
        return ResponseEntity.ok("Dados guardados no InfluxDB!"); // 2. Responder ao Agente
    }
}
