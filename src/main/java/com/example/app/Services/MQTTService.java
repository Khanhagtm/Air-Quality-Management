package com.example.app.Services;

import com.example.app.Models.User;
import com.example.app.Repositories.ThresholdRepo;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MQTTService {
    @Autowired
    private ThresholdRepo thresholdRepo;

    @Autowired
    @Qualifier("serviceMqttClient")
    private MqttClient mqttClient;
    public void sendCommandToDevice(String serialNumber, String command) {
        try {
            if (!mqttClient.isConnected()) {
                mqttClient.connect();
            }
            String topic = serialNumber + "/alert";
            System.out.println(topic);
            mqttClient.publish(topic, new MqttMessage(command.getBytes()));
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public boolean checkDataExceedThreshold(String field, String data, User user) {
        List<Integer> threshold = thresholdRepo.getThresholdDataFromDB(field, user.getId());
        return Integer.parseInt(data.trim().replaceAll("\r", "").replaceAll("\"", "")) > threshold.get(0);
    }
}