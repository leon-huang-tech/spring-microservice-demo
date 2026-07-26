package com.demo.ai.service;

import com.demo.ai.config.AiConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AiService {
    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final ObjectMapper objectMapper;
    private final Map<String, ChatMemory> memories = new ConcurrentHashMap<>();

    @Value("${spring.ai.ollama.chat.options.model:llama3.1:latest}")
    private String model;
    
    public AiService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ChatMemory getOrCreateMemory(String sessionId) {
        return memories.computeIfAbsent(sessionId, k ->
                MessageWindowChatMemory.builder()
                        .chatMemoryRepository(new InMemoryChatMemoryRepository())
                        .maxMessages(AiConstants.MAX_MEMORY_MESSAGES)
                        .build());
    }

    /**
     * serializes each chunk via ObjectMapper before emitting.
     * Fix:
     * Leading spaces in tokens were being swallowed because they were indistinguishable from the
     * "data: " protocol separator, causing words to render without spaces on the frontend.
     */
    public String toJson(String content) {
        try {
            return objectMapper.writeValueAsString(Map.of("content", content));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize chunk to JSON", e);
            return "{\"content\":\"\"}";
        }
    }

    public String getModel() {
        return model;
    }

    public void clearMemory(String sessionId) {
        memories.remove(sessionId);
    }
}
