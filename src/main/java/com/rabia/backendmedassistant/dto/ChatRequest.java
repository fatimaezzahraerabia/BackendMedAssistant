package com.rabia.backendmedassistant.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String message;
    private String userId;
}
