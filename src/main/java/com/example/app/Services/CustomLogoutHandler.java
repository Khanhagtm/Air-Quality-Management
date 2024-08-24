package com.example.app.Services;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomLogoutHandler implements LogoutHandler {

    private final MqttClient mqttClient;

    public CustomLogoutHandler(@Qualifier("controllerMqttClient") MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    @Override
    public void logout(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, Authentication authentication) {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
                mqttClient.close();
                System.out.println("MQTT client disconnected.");
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
}