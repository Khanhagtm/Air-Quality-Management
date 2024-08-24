package com.example.app.Controllers;

import com.example.app.DTOs.LoginDto;
import com.example.app.DTOs.SignUpDto;
import com.example.app.Models.Role;
import com.example.app.Models.User;
import com.example.app.Repositories.RoleRepository;
import com.example.app.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;

@RestController
@RequestMapping("")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/signin")
    public void authenticateUser(@RequestBody LoginDto loginDto, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getUsernameOrEmail(), loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        Authentication authentication1 = SecurityContextHolder.getContext().getAuthentication();

        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (authentication1 != null && authentication1.isAuthenticated()) {
            // Lấy ra tên người dùng từ đối tượng Authentication
            String username = authentication1.getName();
            System.out.println(username);
        } else {
            System.out.println("No user is currently logged in.");
        }
        RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        System.out.println("Redirecting to: ");
        redirectStrategy.sendRedirect(request, response, "/welcome");
    }

//    @PostMapping("/signin")
//    public ResponseEntity<String> authenticateUser(@RequestBody LoginDto loginDto){
//        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
//                loginDto.getUsernameOrEmail(), loginDto.getPassword()));
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        return new ResponseEntity<>("User signed-in successfully!.", HttpStatus.OK);
//    }

    @PostMapping(value = "/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignUpDto signUpDto){

        // add check for username exists in a DB
        if(userRepository.existsByUsername(signUpDto.getUsername())){
            return new ResponseEntity<>("Username is already taken!", HttpStatus.BAD_REQUEST);
        }

        // add check for email exists in DB
        if(userRepository.existsByEmail(signUpDto.getEmail())){
            return new ResponseEntity<>("Email is already taken!", HttpStatus.BAD_REQUEST);
        }

        // create user object
        User user = new User();
        user.setName(signUpDto.getName());
        user.setUsername(signUpDto.getUsername());
        user.setEmail(signUpDto.getEmail());
        user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
        user.setSerialNumber(signUpDto.getSerialNumber());
        user.setCOThreshold(70);
        user.setGasThreshold(700);

        Role roles = roleRepository.findByName("ROLE_USER").get();
        user.setRoles(Collections.singleton(roles));


        userRepository.save(user);

        return new ResponseEntity<>("User registered successfully", HttpStatus.OK);

    }
}