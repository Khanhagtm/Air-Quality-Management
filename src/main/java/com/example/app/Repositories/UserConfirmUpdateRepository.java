package com.example.app.Repositories;

import com.example.app.Models.Firmware;
import com.example.app.Models.User;
import com.example.app.Models.UserConfirmUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserConfirmUpdateRepository extends JpaRepository<UserConfirmUpdate, Long> {
    Optional<UserConfirmUpdate> findByUserAndFirmware(User user, Firmware firmware);
    List<UserConfirmUpdate> findTop5ByUserOrderByTimestampDesc(User user);
}
