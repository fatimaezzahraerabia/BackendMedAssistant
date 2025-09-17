package com.rabia.backendmedassistant.service;

import com.rabia.backendmedassistant.dto.ChatRequest;
import com.rabia.backendmedassistant.dto.ChatResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatService {

    private final WebClient ragServiceClient;
    private final WebClient diagnosisServiceClient;
    private final Map<String, String> userSessionState = new ConcurrentHashMap<>();
    public ChatService(WebClient.Builder webClientBuilder) {
        this.ragServiceClient = webClientBuilder
                .baseUrl("http://localhost:5002/api/rag")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.diagnosisServiceClient = webClientBuilder
                .baseUrl("http://localhost:5001/api/diagnosis")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public ChatResponse getRagResponse(ChatRequest request) {
        String userMessage = request.getMessage();
        String sessionId = request.getUserId();

        if (sessionId == null || sessionId.trim().isEmpty()) {
            // Handle cases where userId is not provided, maybe return an error or use a default session
            sessionId = "default_guest_session";
        }

        String lowerCaseMessage = userMessage.toLowerCase();

        if (lowerCaseMessage.contains("diagnostic") || lowerCaseMessage.contains("conseils")) {
            userSessionState.put(sessionId, "AWAITING_SYMPTOMS");
            return callDiagnosisService(userMessage, sessionId);
        } else {
            String currentState = userSessionState.getOrDefault(sessionId, "START");
            if (currentState.equals("AWAITING_SYMPTOMS")) {
                return callDiagnosisService(userMessage, sessionId);
            } else {
                return callRagService(userMessage);
            }
        }
    }

    private ChatResponse callRagService(String message) {
        Map<String, String> requestBody = Collections.singletonMap("query", message);
        try {
            Mono<ChatResponse> responseMono = this.ragServiceClient.post()
                    .uri("/query")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .map(responseMap -> new ChatResponse((String) responseMap.get("message")));
            return responseMono.block();
        } catch (Exception e) {
            System.err.println("Error calling RAG service: " + e.getMessage());
            return new ChatResponse("The RAG AI service is currently unavailable.");
        }
    }

    private ChatResponse callDiagnosisService(String message, String sessionId) {
        Map<String, String> requestBody = Map.of("message", message, "session_id", sessionId);
        try {
            Mono<ChatResponse> responseMono = this.diagnosisServiceClient.post()
                    .uri("/gemini_chat")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .map(responseMap -> new ChatResponse((String) responseMap.get("message")));
            return responseMono.block();
        } catch (Exception e) {
            System.err.println("Error calling Diagnosis service: " + e.getMessage());
            return new ChatResponse("The Diagnosis AI service is currently unavailable.");
        }
    }
}
