package com.example.app.Models;


import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "device_informations")
public class DeviceInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "serial_number", nullable = false, unique = true)
    private String serialNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "current_ip")
    private String currentIp;

    @Column(name = "record_ip_time")
    private Timestamp recordIpTime;

    @Column(name = "chip_id")

    private String chipId;

    @Column(name = "firmware_version")
    private String firmwareVersion;

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCurrentIp() {
        return currentIp;
    }

    public void setCurrentIp(String currentIp) {
        this.currentIp = currentIp;
    }

    public Timestamp getRecordIpTime() {
        return recordIpTime;
    }

    public void setRecordIpTime(Timestamp recordIpTime) {
        this.recordIpTime = recordIpTime;
    }

    public String getChipId() {
        return chipId;
    }

    public void setChipId(String chipId) {
        this.chipId = chipId;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }
}