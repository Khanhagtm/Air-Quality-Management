package com.example.app.Services;

import com.example.app.Models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        User principal = (User) authentication.getPrincipal();

        // Kiểm tra và xác định người dùng đã đăng nhập thành công
        String redirectUrl = "/api/auth/welcome";
        System.out.println("Redirecting to: " + redirectUrl);
        // Thực hiện chuyển hướng
        response.sendRedirect(redirectUrl);
    }
}