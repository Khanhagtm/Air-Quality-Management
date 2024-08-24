package com.example.app.Controllers;

import com.example.app.Models.*;
import com.example.app.Repositories.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class MessageHanderController {
    @Autowired
    private SimpMessagingTemplate template;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ThresholdRepo thresholdRepo;
    @Autowired
    private SensorDataRepo sensorDataRepo;
    @Autowired
    private ObjectMapper objectMapper; // Inject ObjectMapper
    @Autowired
    private FirmwareRepository firmwareRepository;

    @Autowired
    private UserConfirmUpdateRepository userConfirmUpdateRepository;
    @Autowired
    @Qualifier("controllerMqttClient")
    private MqttClient mqttClient;


    @GetMapping("/index")
    public void sendDisplayDataReceivedFromMQTT(Model model, @RequestParam(required = false, defaultValue = "300") int interval) {
        try {
            if (!mqttClient.isConnected()) {
                mqttClient.connect();
            }

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Object principal = auth.getPrincipal();
            String username = null;
            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            } else {
                username = principal.toString();
            }
            System.out.println(username);
            User user = userRepository.findByUsernameOrEmail(username,username).orElse(null); // Lấy thông tin user từ cơ sở dữ liệu
            if(user != null){
                Map<String,String> topics = new HashMap<>();
                topics.put(user.getSerialNumber() + "/temperature","/topic/temperature");
                topics.put(user.getSerialNumber() + "/humidity","/topic/humidity");
                topics.put(user.getSerialNumber() + "/gas","/topic/gas");
                topics.put(user.getSerialNumber() + "/CO","/topic/co");
                for( Map.Entry<String, String> topic : topics.entrySet()){
                    final String topicName = topic.getKey();
                    final String topicValue = topic.getValue();
                    mqttClient.subscribe(topicName, new IMqttMessageListener() {
                        @Override
                        public void messageArrived(String topic, MqttMessage message) {
                            String parameter = new String(message.getPayload());
                            String value = parameter.trim().replaceAll("\r", "").replaceAll("\"", "");
                            System.out.println("Received message: " + value + " on topic: " + topic);
                            switch(topicValue){
                                case "/topic/gas" :
                                    if(checkDataExceedThreshold("gas_threshold",value,user)){
                                        System.out.println("Gas: " + value);
                                        template.convertAndSend("/threshold/gas", value); // Gửi dữ liệu tới WebSocket endpoint

//                                        sendWarningToGmail("gas_threshold",parameter);
                                    }
                                    break;
                                case "/topic/CO" :
                                    if(checkDataExceedThreshold("CO_threshold",value,user)){
                                        template.convertAndSend("/threshold/CO", value);
                                        System.out.println("CO: " + value);
//                                        sendWarningToGmail("CO_threshold",parameter);
                                    }
                                    break;
                                default:
                            }
                            template.convertAndSend(topicValue, value); // Gửi dữ liệu tới WebSocket endpoint
                        }
                    });
                }

                String serialNumber = user.getSerialNumber();
                LocalDateTime endDateTime = LocalDateTime.now();
                LocalDateTime startDateTime = endDateTime.minusDays(1);

                List<SensorData> sensorDataList = sensorDataRepo.getSensorDataWithSamplingFrequency(serialNumber, startDateTime, endDateTime, interval);
                try {
                    String sensorDataJson = objectMapper.writeValueAsString(sensorDataList);
                    model.addAttribute("sensorDataJson", sensorDataJson);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    model.addAttribute("sensorDataJson", "[]"); // Fallback to an empty JSON array
                }

                LocalDate endDate = LocalDate.now();
                LocalDate startDate = endDate.minusDays(7);

                List<DailyStatistics> statistics = sensorDataRepo.findStatisticsBySerialNumberAndDateRange(serialNumber, startDate, endDate);
                model.addAttribute("statistics", statistics);

                List<UserConfirmUpdate> notifications = userConfirmUpdateRepository.findTop5ByUserOrderByTimestampDesc(user);
                model.addAttribute("notifications", notifications);

                String userName = user.getName();
                boolean isAdmin = user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

                String userRole = isAdmin ? "Admin" : "User";
                model.addAttribute("username", userName);
                model.addAttribute("userRole",userRole);
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public boolean checkDataExceedThreshold(String field,String data,User user){
        List<Integer> threshold = thresholdRepo.getThresholdDataFromDB(field,user.getId());
        return Integer.parseInt(data.trim().replaceAll("\r","").replaceAll("\"","")) > threshold.get(0);
    }

    @GetMapping("/test")
    public String test(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        String username = null;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        User user = userRepository.findByUsernameOrEmail(username, username).orElse(null);
        if (user != null) {
            String serialNumber = user.getSerialNumber();
            List<SensorData> sensorDataList = sensorDataRepo.getSensorDataBySerialNumber(serialNumber);
            try {
                String sensorDataJson = objectMapper.writeValueAsString(sensorDataList);
                model.addAttribute("sensorDataJson", sensorDataJson);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                model.addAttribute("sensorDataJson", "[]"); // Fallback to an empty JSON array
            }
        }

        return "test"; // Trả về tên của view template (ví dụ: test.html)
    }

    @GetMapping("/statistics")
    public String showStatistics(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        String username = null;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        User user = userRepository.findByUsernameOrEmail(username, username).orElse(null);
        if (user != null) {
            String serialNumber = user.getSerialNumber();
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(7);

            List<DailyStatistics> statistics = sensorDataRepo.findStatisticsBySerialNumberAndDateRange(serialNumber, startDate, endDate);
            model.addAttribute("statistics", statistics);
        }

        return "statistics";
    }

    @GetMapping("/firmware/details/{id}")
    public String getFirmwareDetails(@PathVariable Long id, Model model) {
        Optional<UserConfirmUpdate> optionalUserConfirmUpdate = userConfirmUpdateRepository.findById(id);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        String username = null;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        System.out.println(username);
        User user = userRepository.findByUsernameOrEmail(username,username).orElse(null);
        if (optionalUserConfirmUpdate.isPresent()) {
            UserConfirmUpdate userConfirmUpdate = optionalUserConfirmUpdate.get();
            DeviceInformation deviceInformation = userConfirmUpdate.getUser().getDeviceInformations()
                    .stream()
                    .findFirst()
                    .orElse(null);
            String currentVersion = deviceInformation != null ? deviceInformation.getFirmwareVersion() : "N/A";
            model.addAttribute("userConfirmUpdate", id);
            model.addAttribute("firmware", userConfirmUpdate.getFirmware());
            model.addAttribute("firmwareStatus", userConfirmUpdate.getStatus() ? "Confirmed" : "Not Confirmed");
            model.addAttribute("currentVersion", currentVersion);
            List<UserConfirmUpdate> notifications = userConfirmUpdateRepository.findTop5ByUserOrderByTimestampDesc(user);
            model.addAttribute("notifications", notifications);
            return "firmware-details";
        } else {
            return "redirect:/index"; // Nếu không tìm thấy, chuyển hướng về trang chủ
        }
    }

    @PostMapping("/firmware/confirm/{id}")
    public String confirmFirmwareUpdate(@PathVariable Long id) {
        Optional<UserConfirmUpdate> optionalUserConfirmUpdate = userConfirmUpdateRepository.findById(id);
        if (optionalUserConfirmUpdate.isPresent()) {
            UserConfirmUpdate userConfirmUpdate = optionalUserConfirmUpdate.get();
            userConfirmUpdate.setStatus(true);
            userConfirmUpdateRepository.save(userConfirmUpdate);
        }
        return "redirect:/index"; // Sau khi cập nhật thành công, chuyển hướng về trang chủ
    }

}
