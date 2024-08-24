package com.example.app.Controllers;

import com.example.app.Models.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class ChatController {

    @MessageMapping("/chat.sendMessage")
    @SendToUser("/queue/specific-user")
    public ChatMessage sendMessage(Principal principal, @Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        String receiverUsername = chatMessage.getReceiver();
        String senderUsername = principal.getName();
        chatMessage.setSender(senderUsername);

        // Lưu tin nhắn vào cơ sở dữ liệu
        saveMessage(chatMessage);

        // Gửi tin nhắn tới người nhận
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendToUser("/queue/specific-user")
    public ChatMessage addUser(Principal principal, @Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        chatMessage.setType(ChatMessage.MessageType.JOIN);
        chatMessage.setSender(principal.getName());
        return chatMessage;
    }

    private void saveMessage(ChatMessage chatMessage) {
        // Implement lưu tin nhắn vào cơ sở dữ liệu
    }

    @GetMapping("/chat")
    public String threadhold(){
        return "chat";
    }
}
