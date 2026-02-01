//Este codigo serve para definir como os dados vindos a partir do python são guardados

/*
O Processo (Deserialização):

    O Python envia texto (JSON): { "hostName": "PC-01", "latencyMs": 15 }

    O Spring Boot (Backend) recebe o texto.

    O Spring Boot pergunta: "Onde é que eu guardo o valor 'PC-01'?"

    O DTO responde: "Guarda isso na variável String hostName que eu defini."
*/

package com.echostate.monitor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data //serve para evitar fazer codigo repetitivo 
public class NetworkMetricDTO {
    // O Python envia "host_name", nós guardamos em "hostName"
    @JsonProperty("host_name")
    private String hostName;
    @JsonProperty("target_service")
    private String targetService;
    @JsonProperty("latency_ms")
    private Long latencyMs;
    @JsonProperty("status")
    private String status;

}
