package com.vineyard.analytics.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.vineyard.analytics.model.DataPoint;
import com.vineyard.analytics.model.SensorData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço responsável por escrever e consultar séries temporais no InfluxDB.
 *
 * Measurement: vineyard_sensors
 * Tags: device_id
 * Fields: soil_moisture, air_temperature, air_humidity
 */
@Service
public class TimeSeriesService {

    private static final Logger log = LoggerFactory.getLogger(TimeSeriesService.class);

    private static final String MEASUREMENT = "vineyard_sensors";

    private final InfluxDBClient influxDBClient;
    private final String bucket;
    private final String org;

    public TimeSeriesService(InfluxDBClient influxDBClient,
                             @Value("${influxdb.bucket}") String bucket,
                             @Value("${influxdb.org}") String org) {
        this.influxDBClient = influxDBClient;
        this.bucket = bucket;
        this.org = org;
    }

    /**
     * Escreve um ponto de dados no InfluxDB com os valores do sensor.
     */
    public void writePoint(SensorData data) {
        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();

        Point point = Point.measurement(MEASUREMENT)
                .addTag("device_id", data.deviceId())
                .addField("soil_moisture", data.soilMoisture())
                .addField("air_temperature", data.airTemperature())
                .addField("air_humidity", data.airHumidity())
                .time(data.timestamp(), WritePrecision.MS);

        writeApi.writePoint(bucket, org, point);

        log.debug("📊 Ponto escrito no InfluxDB: device={}, moisture={}, temp={}, humidity={}",
                data.deviceId(), data.soilMoisture(), data.airTemperature(), data.airHumidity());
    }

    /**
     * Consulta a média de um campo específico para um device nas últimas N horas.
     *
     * @param deviceId ID do dispositivo
     * @param field    nome do field (e.g., "air_humidity", "air_temperature")
     * @param hours    janela temporal em horas
     * @return valor médio, ou null se não houver dados
     */
    public Double queryMean(String deviceId, String field, int hours) {
        String flux = String.format(
                "from(bucket: \"%s\")" +
                " |> range(start: -%dh)" +
                " |> filter(fn: (r) => r._measurement == \"%s\")" +
                " |> filter(fn: (r) => r.device_id == \"%s\")" +
                " |> filter(fn: (r) => r._field == \"%s\")" +
                " |> mean()",
                bucket, hours, MEASUREMENT, deviceId, field
        );

        QueryApi queryApi = influxDBClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(flux, org);

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Object value = record.getValue();
                if (value instanceof Number number) {
                    return number.doubleValue();
                }
            }
        }

        return null;
    }

    /**
     * Conta o número de pontos de dados para um device nas últimas N horas.
     * Útil para verificar se há dados suficientes para avaliar risco.
     *
     * @param deviceId ID do dispositivo
     * @param hours    janela temporal em horas
     * @return número de pontos de dados
     */
    public long queryCount(String deviceId, String field, int hours) {
        String flux = String.format(
                "from(bucket: \"%s\")" +
                " |> range(start: -%dh)" +
                " |> filter(fn: (r) => r._measurement == \"%s\")" +
                " |> filter(fn: (r) => r.device_id == \"%s\")" +
                " |> filter(fn: (r) => r._field == \"%s\")" +
                " |> count()",
                bucket, hours, MEASUREMENT, deviceId, field
        );

        QueryApi queryApi = influxDBClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(flux, org);

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Object value = record.getValue();
                if (value instanceof Number number) {
                    return number.longValue();
                }
            }
        }

        return 0;
    }

    /**
     * Consulta o histórico de dados para um campo específico de um device.
     *
     * @param deviceId ID do dispositivo
     * @param field    campo a consultar
     * @param range    intervalo temporal (ex: "-24h", "-1h")
     * @return lista de pontos de dados
     */
    public List<DataPoint> getHistory(String deviceId, String field, String range) {
        String flux = String.format(
                "from(bucket: \"%s\")" +
                " |> range(start: %s)" +
                " |> filter(fn: (r) => r._measurement == \"%s\")" +
                " |> filter(fn: (r) => r.device_id == \"%s\")" +
                " |> filter(fn: (r) => r._field == \"%s\")" +
                " |> yield(name: \"mean\")",
                bucket, range, MEASUREMENT, deviceId, field
        );

        QueryApi queryApi = influxDBClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(flux, org);

        List<DataPoint> points = new ArrayList<>();
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Instant time = record.getTime();
                Object value = record.getValue();
                if (value instanceof Number number) {
                    points.add(new DataPoint(time, number.doubleValue()));
                }
            }
        }
        return points;
    }
}
