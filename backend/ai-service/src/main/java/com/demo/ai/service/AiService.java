package com.demo.ai.service;

import com.demo.ai.config.AiConstants;
import com.demo.ai.dto.ChatProfile;
import com.demo.ai.dto.ChatRequest;
import com.demo.ai.dto.ChatResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AiService {
    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final ChatClientFactory chatClientFactory;
    private final ObjectMapper objectMapper;
    private final Map<String, ChatMemory> memories = new ConcurrentHashMap<>();

    @Value("${spring.ai.ollama.chat.options.model:llama3.1:latest}")
    private String model;

    public AiService(ChatClientFactory chatClientFactory, ObjectMapper objectMapper) {
        this.chatClientFactory = chatClientFactory;
        this.objectMapper = objectMapper;
    }

    private ChatMemory getOrCreateMemory(String sessionId) {
        return memories.computeIfAbsent(sessionId, k ->
                MessageWindowChatMemory.builder()
                        .chatMemoryRepository(new InMemoryChatMemoryRepository())
                        .maxMessages(AiConstants.MAX_MEMORY_MESSAGES)
                        .build());
    }

    /*
     * Note:
     * call() is synchronous, so try/catch can catch exceptions directly.
     * stream() is reactive, where exceptions occur after Flux subscription,
     * so onErrorResume must be used instead of try/catch (try/catch cannot catch exceptions inside the async pipeline).
     */
    public ChatResponse chat(ChatRequest chatRequest) {
        ChatMemory memory = getOrCreateMemory(chatRequest.sessionId());
        try {
            ChatClient chatClient = chatClientFactory.create(chatRequest, ChatProfile.CHAT_RAG);
            String response = chatClient.prompt()
                    .user(chatRequest.message())
                    .advisors(MessageChatMemoryAdvisor.builder(memory).build())
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatRequest.sessionId()))
                    .call()
                    .content();
            return new ChatResponse(response, model);
        } catch (Exception e) {
            log.error("AI service error: {}", e.getMessage(), e);
            String userMessage;
            if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
                userMessage = "AI service is currently unavailable. Please try again later.";
            } else {
                userMessage = "Error: " + e.getMessage();
            }
            return new ChatResponse(userMessage, model);
        }
    }

    /*
     * Note:
     * call() is synchronous, so try/catch can catch exceptions directly.
     * stream() is reactive, where exceptions occur after Flux subscription,
     * so onErrorResume must be used instead of try/catch (try/catch cannot catch exceptions inside the async pipeline).
     */
    public Flux<String> chatStream(ChatRequest chatRequest) {
        ChatMemory memory = getOrCreateMemory(chatRequest.sessionId());
        ChatClient chatClient = chatClientFactory.create(chatRequest, ChatProfile.CHAT_RAG);
        return chatClient.prompt()
         .user(chatRequest.message())
         .advisors(MessageChatMemoryAdvisor.builder(memory).build())
         .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatRequest.sessionId()))
         .stream()
         .content()
         .doOnNext(token -> log.debug("chunk: '{}'", token))
         // (Fix 'preserve whitespace' )serializes each chunk via ObjectMapper before emitting
         .map(this::toJson)
         .onErrorResume(e -> {
             log.error("AI service stream error: {}", e.getMessage(), e);
             String userMessage;
             if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
                 userMessage = "AI service is currently unavailable. Please try again later.";
             } else {
                 userMessage = "Error: " + e.getMessage();
             }
             return Flux.just(toJson(userMessage));
         });
    }

    /**
     * serializes each chunk via ObjectMapper before emitting.
     * Fix:
     * Leading spaces in tokens were being swallowed because they were indistinguishable from the
     * "data: " protocol separator, causing words to render without spaces on the frontend.
     */
    private String toJson(String content) {
        try {
            return objectMapper.writeValueAsString(Map.of("content", content));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize chunk to JSON", e);
            return "{\"content\":\"\"}";
        }
    }
    public void clearMemory(String sessionId) {
        memories.remove(sessionId);
    }
}
