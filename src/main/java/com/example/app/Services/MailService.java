package com.example.app.Services;

import com.example.app.DTOs.DataMailDTO;
import jakarta.mail.MessagingException;

public interface MailService {
    void sendHtmlMail(DataMailDTO dataMail, String templateName) throws MessagingException;
}
