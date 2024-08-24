package com.example.app.Controllers;

import com.example.app.DTOs.UserInfoDTO;
import com.example.app.Models.User;
import com.example.app.Models.UserConfirmUpdate;
import com.example.app.Repositories.ThresholdRepo;
import com.example.app.Repositories.UserConfirmUpdateRepository;
import com.example.app.Repositories.UserRepository;
import com.example.app.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class SettingController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private UserConfirmUpdateRepository userConfirmUpdateRepository;

    @GetMapping("/setting")
    public String showSettingForm(Model model) {
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
            model.addAttribute("username", user.getUsername());
            model.addAttribute("email", user.getEmail());
            model.addAttribute("coThreshold", user.getCOThreshold());
            model.addAttribute("gasThreshold", user.getGasThreshold());
            List<UserConfirmUpdate> notifications = userConfirmUpdateRepository.findTop5ByUserOrderByTimestampDesc(user);
            model.addAttribute("notifications", notifications);
            String userName = user.getName();
            boolean isAdmin = user.getRoles().stream()
                    .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

            String userRole = isAdmin ? "Admin" : "User";
            model.addAttribute("userName", userName);
            model.addAttribute("userRole",userRole);
        }

        return "setting";
    }

    @PostMapping("/setting")
    public String updateSetting(@RequestParam("username") String username,
                                @RequestParam("oldPassword") String oldPassword,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("coThreshold") Integer  coThreshold,
                                @RequestParam("gasThreshold") Integer  gasThreshold,
                                Model model) {
        User user = userRepository.findByUsernameOrEmail(username, username).orElse(null);
        if (user != null) {
            if (userService.checkPassword(user, oldPassword)) {
                userService.updateUser(user, newPassword, coThreshold, gasThreshold);
                model.addAttribute("message", "Update successful");
            } else {
                model.addAttribute("message", "Old password is incorrect");
            }
            model.addAttribute("username", user.getUsername());
            model.addAttribute("email", user.getEmail());
            model.addAttribute("coThreshold", user.getCOThreshold());
            model.addAttribute("gasThreshold", user.getGasThreshold());
            List<UserConfirmUpdate> notifications = userConfirmUpdateRepository.findTop5ByUserOrderByTimestampDesc(user);
            model.addAttribute("notifications", notifications);
            String userName = user.getName();
            boolean isAdmin = user.getRoles().stream()
                    .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

            String userRole = isAdmin ? "Admin" : "User";
            model.addAttribute("userName", userName);
            model.addAttribute("userRole",userRole);
        }
        return "setting";
    }
}
