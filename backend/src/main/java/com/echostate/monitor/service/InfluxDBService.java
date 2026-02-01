package com.echostate.monitor.service;

import com.echostate.monitor.dto.NetworkMetricDTO;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import jakarta.annotation.PostConstruct;
import jdk.jshell.spi.ExecutionControl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class InfluxDBService {
    // Ler as configurações do application.properties
    @Value("${influx.url}")
    private String url;

    @Value("${influx.token}")
    private String token;

    @Value("${influx.org}")
    private String org;

    @Value("${influx.bucket}")
    private String bucket;

    private WriteApiBlocking writeApi;

    @PostConstruct
    public void init(){
        //Conectar ao influxDB
        InfluxDBClient client = InfluxDBClientFactory.create(url,token.toCharArray(),org, bucket);
        this.writeApi = client.getWriteApiBlocking();
        System.out.println("Conexão ao InfluxDB estabelecida!");
    }

    // Metodo para converter o nosso DTO num "Ponto" do InfluxDB e gravar
    public void writeMetric(NetworkMetricDTO metric){
        try{
            // Validar que temos dados antes de gravar
            if(metric.getLatencyMs() == null){
                System.err.println("Métrica inválida - latência é null");
                return;
            }
            
            if(metric.getHostName() == null || metric.getTargetService() == null || metric.getStatus() == null){
                System.err.println("Métrica inválida - campos obrigatórios são null");
                return;
            }
            
            // Se latência for 0, significa DOWN - usar 0 mesmo
            long latencyValue = metric.getLatencyMs();
            
            Point point = Point.measurement("network_latency")
                    .addTag("host", metric.getHostName())
                    .addTag("target", metric.getTargetService())
                    .addTag("status", metric.getStatus())
                    .addField("latency_ms", latencyValue)
                    .time(Instant.now(), WritePrecision.MS);

            // Debug: ver o ponto antes de gravar
            System.out.println("Ponto criado: " + point.toLineProtocol());
            
            writeApi.writePoint(point);
            System.out.println("Gravado no InfluxDB com sucesso!");

        }catch(Exception e){
            System.err.println("Erro ao gravar no InfluxDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
