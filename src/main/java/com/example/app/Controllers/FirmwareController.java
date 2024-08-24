package com.example.app.Controllers;

import com.example.app.Models.DeviceInformation;
import com.example.app.Models.Firmware;
import com.example.app.Models.User;
import com.example.app.Models.UserConfirmUpdate;
import com.example.app.Repositories.DeviceInformationRepository;
import com.example.app.Repositories.FirmwareRepository;
import com.example.app.Repositories.UserConfirmUpdateRepository;
import com.example.app.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@RestController
public class FirmwareController {

    private final Path firmwareLocation = Paths.get("/firmware");

    @Autowired
    private FirmwareRepository firmwareRepository;

    @Autowired
    private DeviceInformationRepository deviceRepository;

    @Autowired
    private UserConfirmUpdateRepository userConfirmUpdateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeviceInformationRepository deviceInformationRepository;

    @GetMapping("/esp/update/arduino")
    public ResponseEntity<?> handleUpdate(@RequestHeader Map<String, String> headers) {

        String userAgent = headers.getOrDefault("user-agent", "");
        if (!userAgent.equals("ESP8266-http-Update")) {
            return ResponseEntity.status(403).body("Only for ESP8266 updater!");
        }

        if (!headers.containsKey("x-esp8266-mode") || !headers.containsKey("x-esp8266-sta-mac")
                || !headers.containsKey("x-esp8266-ap-mac") || !headers.containsKey("x-esp8266-free-space")
                || !headers.containsKey("x-esp8266-sketch-size") || !headers.containsKey("x-esp8266-sketch-md5")
                || !headers.containsKey("x-esp8266-chip-size") || !headers.containsKey("x-esp8266-sdk-version")) {
            return ResponseEntity.status(403).body("Only for ESP8266 updater! (header missing)");
        }

        String chipId = headers.get("x-esp8266-chip-id");
        String mode = headers.get("x-esp8266-mode");

        try {
            String version = headers.getOrDefault("x-esp8266-version" , "");
            Optional<Firmware> optionalFirmware = firmwareRepository.findFirstByOrderByVersionDesc();;
            if (optionalFirmware.isEmpty()) {
                return ResponseEntity.status(404).body("Firmware not found");
            }

            Firmware firmware = optionalFirmware.get();
            Path file = firmwareLocation.resolve(firmware.getFileName()).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.status(404).body("File not found");
            }

            // Check if user confirmed the update
            Optional<DeviceInformation> deviceOptional = deviceRepository.findByChipId(chipId);
            if (deviceOptional.isPresent()) {
                DeviceInformation device = deviceOptional.get();
                User user = device.getUser(); // Assuming a device is associated with a user
                if (user != null) {
                    boolean confirmed = getUserConfirmStatus(user, firmware);
                    if (!confirmed) {
                        return ResponseEntity.status(403).body("User has not confirmed the update");
                    }
                }
            } else {
                return ResponseEntity.status(404).body("Device not found");
            }

            if ("sketch".equals(mode)) {
                if (version != null && !version.equals(firmware.getVersion())) {
                    updateFirmwareVersion(deviceOptional.get().getId(),firmware.getVersion());
                    return sendFile(resource, firmware.getVersion());
                } else {
                    return ResponseEntity.status(304).body("File not modified");
                }
            } else if ("version".equals(mode)) {
                return ResponseEntity.ok()
                        .header("x-version", firmware.getVersion())
                        .body("Version check");
            } else {
                return ResponseEntity.status(404).body("Mode not supported");
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.status(404).body("File not found");
        }
    }

    private ResponseEntity<Resource> sendFile(Resource resource, String version) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .header("x-version", version)
                .body(resource);
    }

    public void updateFirmwareVersion(Long deviceId, String newFirmwareVersion) {
        DeviceInformation device = deviceInformationRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));
        device.setFirmwareVersion(newFirmwareVersion);
        deviceInformationRepository.save(device);
    }

    // Method to get user confirmation status
    public boolean getUserConfirmStatus(User user, Firmware firmware) {
        try {
            Optional<UserConfirmUpdate> confirmUpdate = userConfirmUpdateRepository.findByUserAndFirmware(user, firmware);
            return confirmUpdate.map(UserConfirmUpdate::getStatus).orElse(false);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmFirmwareUpdate(@RequestParam Long userId, @RequestParam Long firmwareId, @RequestParam boolean status) {
        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Firmware> firmwareOptional = firmwareRepository.findById(firmwareId);

        if (userOptional.isPresent() && firmwareOptional.isPresent()) {
            updateUserConfirmStatus(userOptional.get(), firmwareOptional.get(), status);
            return ResponseEntity.ok().body("Firmware update status updated");
        } else {
            return ResponseEntity.status(404).body("User or Firmware not found");
        }
    }

    // Method to update user confirmation status
    private void updateUserConfirmStatus(User user, Firmware firmware, boolean status) {
        try {
            UserConfirmUpdate confirmUpdate = new UserConfirmUpdate();
            confirmUpdate.setUser(user);
            confirmUpdate.setFirmware(firmware);
            confirmUpdate.setStatus(status);
            confirmUpdate.setTimestamp(new Date()); // Set timestamp
            userConfirmUpdateRepository.save(confirmUpdate);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}