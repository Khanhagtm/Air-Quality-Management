package com.example.app.Repositories;

import com.example.app.Models.Firmware;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FirmwareRepository extends JpaRepository<Firmware, Long> {
    Optional<Firmware> findByVersion(String version);

    Optional<Firmware> findFirstByOrderByVersionDesc();
}