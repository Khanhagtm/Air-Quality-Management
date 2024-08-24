package com.example.app.Services;

import com.example.app.Models.User;
import com.example.app.Repositories.ThresholdRepo;
import com.example.app.Repositories.UserRepository;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@EnableScheduling
public class DataReceivedHandler {
//
//    @Autowired
//    private SimpMessagingTemplate template;
//    @Autowired
//    private UserRepository userRepository;
//    @Autowired
//    private ThresholdRepo thresholdRepo;
//    @Autowired
//    private MqttClient mqttClient;
//
////    @PostConstruct
////    public void init() {
////        Thread thread = new Thread(){
////            public void run(){
////                handleReceivedData();
////            }
////        };
////        thread.start();
////    }
//
////    @Scheduled(initialDelay = 30000,fixedRate = 30000)
//    public void handleReceivedData(){
//        try {
//            if (!mqttClient.isConnected()) {
//                mqttClient.connect();
//            }
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            Object principal = auth.getPrincipal();
//            String username = null;
//            if (principal instanceof UserDetails) {
//                username = ((UserDetails) principal).getUsername();
//            } else {
//                username = principal.toString();
//            }
//            System.out.println(username);
//            User user = userRepository.findByUsernameOrEmail(username,username).orElse(null); // Lấy thông tin user từ cơ sở dữ liệu
//            if(user != null){
//                Map<String,String> topics = new HashMap<>();
//                topics.put(user.getSerialNumber() + "/temperature","/topic/temperature");
//                topics.put(user.getSerialNumber() + "/humidity","/topic/humidity");
//                topics.put(user.getSerialNumber() + "/gas","/topic/gas");
//                topics.put(user.getSerialNumber() + "/co","/topic/co");
//                for( Map.Entry<String, String> topic : topics.entrySet()){
//                    final String topicName = topic.getKey();
//                    final String topicValue = topic.getValue();
//
//                    mqttClient.subscribe(topicName, new IMqttMessageListener() {
//                        @Override
//                        public void messageArrived(String topic, MqttMessage message) {
//                            String parameter = new String(message.getPayload());
//                            System.out.println("Received message: " + parameter);
//                            switch(topicValue){
//                                case "/topic/gas" :
//                                    if(checkDataExceedThreshold("gas_threshold",parameter,user)){
//                                        template.convertAndSend("/threshold/gas", parameter); // Gửi dữ liệu tới WebSocket endpoint
//                                        sendWarningToGmail("gas_threshold",parameter);
//                                    }
//                                    break;
//                                case "/topic/co" :
//                                    if(checkDataExceedThreshold("CO_threshold",parameter,user)){
//                                        template.convertAndSend("/threshold/CO", parameter);
//                                        sendWarningToGmail("CO_threshold",parameter);
//                                    }
//                                    break;
//                            }
//                        }
//                    });
//                }
//            }
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void sendWarningToGmail(String field,String data){
////        TODO
//    }
//
//    public boolean checkDataExceedThreshold(String field,String data,User user){
//        List<Integer> threshold = thresholdRepo.getThresholdDataFromDB(field,user.getId());
//        return Integer.parseInt(data) > threshold.get(0);
//    }
//

}
