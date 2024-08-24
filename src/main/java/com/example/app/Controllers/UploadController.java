package com.example.app.Controllers;

import com.example.app.Models.DeviceInformation;
import com.example.app.Models.Firmware;
import com.example.app.Models.User;
import com.example.app.Models.UserConfirmUpdate;
import com.example.app.Repositories.FirmwareRepository;
import com.example.app.Repositories.UserConfirmUpdateRepository;
import com.example.app.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Controller
public class UploadController {

    private final Path firmwareLocation = Paths.get("/firmware");

    @Autowired
    private FirmwareRepository firmwareRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserConfirmUpdateRepository userConfirmUpdateRepository;

    @GetMapping("/uploadFirmware")
    public String showUploadForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        String username = null;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        User user = userRepository.findByUsernameOrEmail(username,username).orElse(null);
        List<UserConfirmUpdate> notifications = userConfirmUpdateRepository.findTop5ByUserOrderByTimestampDesc(user);
        model.addAttribute("notifications", notifications);
        if(user != null){
            String userName = user.getName();
            boolean isAdmin = user.getRoles().stream()
                    .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

            String userRole = isAdmin ? "Admin" : "User";
            model.addAttribute("username", userName);
            model.addAttribute("userRole",userRole);
        }
        return "upload";
    }

    @PostMapping("/uploadFirmware")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   @RequestParam("version") String version,
                                   @RequestParam("description") String description,
                                   Model model) {
        try {
            if (file.isEmpty()) {
                model.addAttribute("message", "Please select a file to upload");
                return "upload";
            }

            // Create directories if not exist
            Files.createDirectories(firmwareLocation);

            // Save the file
            Path targetLocation = firmwareLocation.resolve(file.getOriginalFilename()).normalize();
            Files.copy(file.getInputStream(), targetLocation);

            // Save the version and file name to the database
            Firmware firmware = new Firmware();
            firmware.setVersion(version);
            firmware.setFileName(file.getOriginalFilename());
            firmware.setCreateAt(LocalDateTime.now());
            firmware.setDescription(description);
            firmwareRepository.save(firmware);

            // Create user confirm update records for all users
            List<User> users = userRepository.findAll();
            for (User user : users) {
                UserConfirmUpdate userConfirmUpdate = new UserConfirmUpdate();
                userConfirmUpdate.setUser(user);
                userConfirmUpdate.setFirmware(firmware);
                userConfirmUpdate.setStatus(false); // Initially set to not confirmed
                userConfirmUpdate.setTimestamp(new Date());
                userConfirmUpdateRepository.save(userConfirmUpdate);
            }

            model.addAttribute("message", "You successfully uploaded '" + file.getOriginalFilename() + "'");
        } catch (IOException e) {
            model.addAttribute("message", "Could not upload the file: " + file.getOriginalFilename() + "!");
        }

        return "upload";
    }
}