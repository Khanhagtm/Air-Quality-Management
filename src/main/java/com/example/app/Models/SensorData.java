package com.example.app.Models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_datas")
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "serial_number", nullable = false)
    private String serialNumber;

    @Column(name = "record_time", nullable = false)
    private LocalDateTime recordTime;

    @Column(name = "sensor_value", nullable = false)
    private double sensorValue;

    @Column(name = "sensor_type", nullable = false)
    private int sensorType;

    // Constructors
    public SensorData() {
    }

    public SensorData(String serialNumber, LocalDateTime recordTime, double sensorValue, int sensorType) {
        this.serialNumber = serialNumber;
        this.recordTime = recordTime;
        this.sensorValue = sensorValue;
        this.sensorType = sensorType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public LocalDateTime getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(LocalDateTime recordTime) {
        this.recordTime = recordTime;
    }

    public double getSensorValue() {
        return sensorValue;
    }

    public void setSensorValue(double sensorValue) {
        this.sensorValue = sensorValue;
    }

    public int getSensorType() {
        return sensorType;
    }

    public void setSensorType(int sensorType) {
        this.sensorType = sensorType;
    }
}