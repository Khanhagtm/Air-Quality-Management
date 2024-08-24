package com.example.app.Services;

import com.example.app.Models.User;
import com.example.app.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean checkPassword(User user, String oldPassword) {
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }

    public void updateUser(User user, String newPassword, int coThreshold, int gasThreshold) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setCOThreshold(coThreshold);
        user.setGasThreshold(gasThreshold);
        userRepository.save(user);
    }
}