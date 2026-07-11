package com.demo.ai.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.demo.ai.config.AiConstants;
import com.demo.ai.dto.ChatResponse;

import reactor.core.publisher.Flux;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final ChatClient chatClient;
    private final Map<String, ChatMemory> memories = new ConcurrentHashMap<>();

    @Value("${spring.ai.ollama.chat.options.model}")
    private String model;

    public AiService(ChatClient.Builder builder, DataService dataService) {
        this.chatClient = builder
        		.defaultSystem("""
        			    You are a helpful assistant for an e-commerce platform.
        				You can query order and user data when the user asks about them.
        				Respond in the same language as the user. Be concise and friendly.
        			    """)
                .defaultTools(dataService)
                .build();
    }

    private ChatMemory getOrCreateMemory(String sessionId) {
        return memories.computeIfAbsent(sessionId, k ->
                MessageWindowChatMemory.builder()
                        .chatMemoryRepository(new InMemoryChatMemoryRepository())
                        .maxMessages(AiConstants.MAX_MEMORY_MESSAGES)
                        .build());
    }

    public ChatResponse chat(String message, String sessionId) {
    	ChatMemory memory = getOrCreateMemory(sessionId);
        try {
            String response = chatClient.prompt()
                    .user(message)
                    .advisors(MessageChatMemoryAdvisor.builder(memory)
                            .conversationId(sessionId)
                            .build())
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

    public Flux<String> chatStream(String message, String sessionId) {
        ChatMemory memory = getOrCreateMemory(sessionId);
        try {
            String response = chatClient.prompt()
                    .user(message)
                    .advisors(MessageChatMemoryAdvisor.builder(memory)
                            .conversationId(sessionId)
                            .build())
                    .call() 
                    .content();
            Flux<String> word = Flux.fromArray(response.split(""))
                    .map(character -> {
                    	log.debug("chunk: '{}'", character);
                        return character;
                    });
            return word; 
        } catch (Exception e) {
            log.error("AI service error: {}", e.getMessage(), e);
            
            String userMessage;
            if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
                userMessage = "AI service is currently unavailable. Please try again later.";
            } else {
                userMessage = "Error: " + e.getMessage();
            }
            return Flux.just(userMessage);
        }
    }
    
    public void clearMemory(String sessionId) {
        memories.remove(sessionId);
    }
}