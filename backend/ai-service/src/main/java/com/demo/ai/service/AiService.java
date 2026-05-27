package com.demo.ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.demo.ai.dto.ChatResponse;

@Service
public class AiService {

    private final ChatClient chatClient;
    
    @Value("${spring.ai.ollama.chat.options.model}")
    private String model;

    public AiService(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("""
                    You are a helpful assistant for an e-commerce platform.
                    You help users with questions about their orders and accounts.
                    Always respond in the same language as the user's message.
                    Be concise, professional, and friendly.
                    """)
                .build();
    }

    public ChatResponse chat(String message) {
        String response = chatClient.prompt()
                .user(message)
                .call()
                .content();
        return new ChatResponse(response, model);
    }
}