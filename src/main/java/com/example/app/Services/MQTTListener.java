package com.example.app.Services;

import com.example.app.DTOs.DataMailDTO;
import com.example.app.Models.User;
import com.example.app.Repositories.SensorDataRepo;
import com.example.app.Repositories.UserRepository;
import com.example.app.Services.utils.Const;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class MQTTListener {

    @Autowired
    @Qualifier("serviceMqttClient")
    private MqttClient mqttClient;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MQTTService mqttService;
    @Autowired
    private SensorDataRepo sensorDataRepo;
    @Autowired
    private MailService mailService;

    // Bản đồ để lưu trữ dữ liệu tạm thời cho mỗi serialNumber
    private final Map<String, List<Map<String, Object>>> dataBufferMap = new ConcurrentHashMap<>();
    private final Map<String, Long> lastInsertedSendMailTimeMap = new ConcurrentHashMap<>();
    private final Map<String, Long> lastInsertedIpAddressTimeMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService dataSaverExecutor = Executors.newScheduledThreadPool(1);
    private final ScheduledExecutorService dataCleanerExecutor = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void init() {
        try {
            // Lấy danh sách serial_number từ database
            Iterable<User> users = userRepository.findAll();
            for (User user : users) {
                String serialNumber = user.getSerialNumber();
                subscribeToTopic(serialNumber);
                dataBufferMap.put(serialNumber, new ArrayList<>()); // Khởi tạo bộ đệm dữ liệu tạm thời
                lastInsertedSendMailTimeMap.put(user.getEmail(), 0L);
                lastInsertedIpAddressTimeMap.put(serialNumber,sensorDataRepo.getRecordIpTime(serialNumber));
            }
            // Đặt lịch lưu dữ liệu mỗi 15 phút
            dataSaverExecutor.scheduleAtFixedRate(this::saveBufferedData, 15, 15, TimeUnit.MINUTES);

            // Đặt lịch xóa dữ liệu mỗi 15 phút
            dataCleanerExecutor.scheduleAtFixedRate(this::deleteOldData, 15, 15, TimeUnit.MINUTES);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribeToTopic(String serialNumber) throws MqttException {
        // Đăng ký lắng nghe các topic liên quan đến serial_number
        String[] topics = {
                serialNumber + "/temperature",
                serialNumber + "/humidity",
                serialNumber + "/gas",
                serialNumber + "/CO",
                serialNumber + "/ipaddr",
        };

        for (String topic : topics) {
            mqttClient.subscribe(topic, (t, message) -> handleMessage(t, new String(message.getPayload())));
        }
    }

    public void handleMessage(String topic, String message) {
        // Xử lý tin nhắn nhận được
        String[] parts = topic.split("/");
        String serialNumber = parts[0];
        String parameterType = parts[1];

        if(Objects.equals(parameterType, "ipaddr")){
            String ipAddr = message.trim().replaceAll("\r", "").replaceAll("\"", "");
            bufferRecordIpData(serialNumber,ipAddr);
        }else{
            double value = Double.parseDouble(message.trim().replaceAll("\r", "").replaceAll("\"", ""));
            switch(parameterType){
                case "temperature":
                    bufferSensorData(serialNumber, 0, value);
                    break;
                case "humidity":
                    bufferSensorData(serialNumber, 1, value);
                    break;
                case "gas":
                    bufferSensorData(serialNumber, 2, value);
                    break;
                case "CO":
                    bufferSensorData(serialNumber, 3, value);
                    break;
            }

            User user = userRepository.findBySerialNumber(serialNumber).orElse(null);
            if (user != null && (Objects.equals(parameterType, "gas") || Objects.equals(parameterType, "CO"))) {
                if (mqttService.checkDataExceedThreshold(parameterType + "_threshold", message, user)) {
                    mqttService.sendCommandToDevice(serialNumber, "ALERT");
                    sendWarningToGmail(user, parameterType, String.valueOf(value));
                }
            }
        }
    }

    public void bufferSensorData(String serialNumber, int parameterType, double value) {
        List<Map<String, Object>> dataBuffer = dataBufferMap.computeIfAbsent(serialNumber, k -> new ArrayList<>());
        Map<String, Object> data = new HashMap<>();
        data.put("serialNumber", serialNumber);
        data.put("parameterType", parameterType);
        data.put("value", value);
        data.put("timestamp", new Timestamp(System.currentTimeMillis()));
        dataBuffer.add(data);
    }

    public void saveBufferedData() {
        List<Map<String, Object>> allBufferedData = new ArrayList<>();

        for (String serialNumber : dataBufferMap.keySet()) {
            List<Map<String, Object>> dataBuffer = dataBufferMap.get(serialNumber);
            if (!dataBuffer.isEmpty()) {
                allBufferedData.addAll(dataBuffer);
                dataBuffer.clear();
            }
        }

        if (!allBufferedData.isEmpty()) {
            sensorDataRepo.bulkInsertSensorData(allBufferedData);
        }
    }

    public void deleteOldData() {
        sensorDataRepo.deleteDataOlderThanOneDay();
    }

    public void sendWarningToGmail(User user ,String parameterType ,String value ){
        try {
            long currentTime = System.currentTimeMillis();
            long lastInsertedTime = lastInsertedSendMailTimeMap.get(user.getEmail());
            if (currentTime - lastInsertedTime >= 60000*15) {
                DataMailDTO dataMail = new DataMailDTO();

                dataMail.setTo(user.getEmail());
                dataMail.setSubject(Const.SEND_MAIL_SUBJECT.CLIENT_REGISTER);

                LocalDateTime timeToSend = LocalDateTime.now();
                Map<String, Object> props = new HashMap<>();
                props.put("name", user.getName());
                props.put("sensorType", parameterType);
                props.put("sensorValue", value);
                props.put("time", timeToSend.toString());
                dataMail.setProps(props);

                mailService.sendHtmlMail(dataMail, Const.TEMPLATE_FILE_NAME.CLIENT_REGISTER);
                lastInsertedSendMailTimeMap.put(user.getEmail(), currentTime); // Cập nhật thời gian lần cuối cùng dữ liệu được lưu trữ
            }
        } catch (MessagingException exp){
            exp.printStackTrace();
        }
    }

    public void bufferRecordIpData(String serialNumber ,String value){
        try{
            long currentTimeMillis  = System.currentTimeMillis();
            long lastInsertedTime = lastInsertedIpAddressTimeMap.get(serialNumber);
            if (currentTimeMillis - lastInsertedTime >= 60000*60 || lastInsertedTime == -1) {
                Timestamp currentTime = new Timestamp(currentTimeMillis);
                sensorDataRepo.updateDeviceInformation(serialNumber,value,currentTime);
                lastInsertedIpAddressTimeMap.put(serialNumber, currentTimeMillis);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}