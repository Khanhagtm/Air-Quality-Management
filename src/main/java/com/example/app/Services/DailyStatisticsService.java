package com.example.app.Services;

import com.example.app.Repositories.SensorDataRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DailyStatisticsService {

    @Autowired
    private SensorDataRepo sensorDataRepo;

    @Scheduled(cron = "0 0 0 * * *") // Chạy vào lúc nửa đêm hàng ngày
    public void calculateAndStoreDailyStatistics() {
        List<String> serialNumbers = sensorDataRepo.findAllSerialNumbers();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime now = LocalDateTime.now();

        for (String serialNumber : serialNumbers) {
            for (int type = 0; type < 4; type++) {
                Double maxValue = sensorDataRepo.findMaxValueByTypeAndSerialNumberAndDate(type, serialNumber, now);
                Double minValue = sensorDataRepo.findMinValueByTypeAndSerialNumberAndDate(type, serialNumber, now);
                Double avgValue = sensorDataRepo.findAvgValueByTypeAndSerialNumberAndDate(type, serialNumber, now);
                if(maxValue != null && minValue != null && avgValue != null){
                    sensorDataRepo.insertDailyStatistics(serialNumber, yesterday, type, maxValue, minValue, avgValue);
                }
            }
        }
    }
}
