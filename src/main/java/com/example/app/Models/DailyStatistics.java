package com.example.app.Models;

import java.time.LocalDate;

public class DailyStatistics {
    private Long id;
    private String serialNumber;
    private LocalDate date;
    private int sensorType;
    private double maxValue;
    private double minValue;
    private double avgValue;

    public DailyStatistics(Long id, String serialNumber, LocalDate date, int sensorType, double maxValue, double minValue, double avgValue) {
        this.id = id;
        this.serialNumber = serialNumber;
        this.date = date;
        this.sensorType = sensorType;
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.avgValue = avgValue;
    }

    // Getters and setters
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getSensorType() {
        return sensorType;
    }

    public void setSensorType(int sensorType) {
        this.sensorType = sensorType;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public double getAvgValue() {
        return avgValue;
    }

    public void setAvgValue(double avgValue) {
        this.avgValue = avgValue;
    }
}
