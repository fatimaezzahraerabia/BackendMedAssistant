package com.rabia.backendmedassistant.controller;

import com.rabia.backendmedassistant.dto.ChatRequest;
import com.rabia.backendmedassistant.dto.ChatResponse;
import com.rabia.backendmedassistant.service.ChatService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
// Allow requests from the default Angular development server.
// For production, you should restrict this to your actual frontend domain.
@CrossOrigin(origins = "http://localhost:4200")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ChatResponse handleChat(@RequestBody ChatRequest request) {
        return chatService.getRagResponse(request);
    }
}
