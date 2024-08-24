package com.example.app.Controllers;

import com.example.app.Models.DeviceInformation;
import com.example.app.Models.User;
import com.example.app.Models.UserConfirmUpdate;
import com.example.app.Repositories.DeviceInformationRepository;
import com.example.app.Repositories.UserConfirmUpdateRepository;
import com.example.app.Repositories.UserRepository;
import com.example.app.Services.DeviceInformationService;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private DeviceInformationService deviceInformationService;
    @Autowired
    private UserConfirmUpdateRepository userConfirmUpdateRepository;
    @Autowired
    private UserRepository userRepository;

    @RequestMapping("")
    public String signin(){
        return "signin";
    }

    @GetMapping("/welcome")
    public String greeting() {
        return "welcome";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/signup")
    public String signup(){
        return "signup";
    }


    @GetMapping("/devices")
    public String getDeviceList(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        String username = null;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        User user = userRepository.findByUsernameOrEmail(username,username).orElse(null);
        List<DeviceInformation> devices = deviceInformationService.getAllDeviceInformations();
        model.addAttribute("devices", devices);
        List<UserConfirmUpdate> notifications = userConfirmUpdateRepository.findTop5ByUserOrderByTimestampDesc(user);
        model.addAttribute("notifications", notifications);
        assert user != null;
        String userName = user.getName();
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        String userRole = isAdmin ? "Admin" : "User";
        model.addAttribute("username", userName);
        model.addAttribute("userRole",userRole);
        return "device_list";
    }
//    @GetMapping("/test")
//    public String test(){
//        return "test";
//    }

}
