package com.example.app.Services;

import com.example.app.Models.DeviceInformation;
import com.example.app.Repositories.DeviceInformationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceInformationService {

    @Autowired
    private DeviceInformationRepository deviceInformationRepository;

    public List<DeviceInformation> getAllDeviceInformations() {
        return deviceInformationRepository.findAll();
    }
}
