package com.example.app.Configurations;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQTTConfig {

    private final String broker = "tcp://139.180.135.86:1883";
    private final String mqttUsername = "admin1";
    private final String mqttPassword = "admin1";

    @Bean(name = "controllerMqttClient")
    public MqttClient controllerMqttClient() throws Exception {
        String clientId = "ControllerJavaMQTTSubscriber";
        MqttClient mqttClient = new MqttClient(broker, clientId, new MemoryPersistence());
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setUserName(mqttUsername);
        connOpts.setPassword(mqttPassword.toCharArray());
        connOpts.setCleanSession(true);
        mqttClient.connect(connOpts);
        return mqttClient;
    }

    @Bean(name = "serviceMqttClient")
    public MqttClient serviceMqttClient() throws Exception {
        String clientId = "ServiceJavaMQTTSubscriber";
        MqttClient mqttClient = new MqttClient(broker, clientId, new MemoryPersistence());
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setUserName(mqttUsername);
        connOpts.setPassword(mqttPassword.toCharArray());
        connOpts.setCleanSession(true);
        mqttClient.connect(connOpts);
        return mqttClient;
    }
}
