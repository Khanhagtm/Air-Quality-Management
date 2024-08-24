package com.example.app.Repositories;

import com.example.app.Models.DeviceInformation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface DeviceInformationRepository extends JpaRepository<DeviceInformation, Long> {
    List<DeviceInformation> findByUserId(Long userId);

    Optional<DeviceInformation> findByChipId(String chipId);
}