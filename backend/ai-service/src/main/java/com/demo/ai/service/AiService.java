package com.demo.ai.service;

import com.demo.ai.dto.ChatResponse;
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

    private final ChatClient chatClient;
    private final Map<String, ChatMemory> memories = new ConcurrentHashMap<>();
    private final DataService dataService;

    @Value("${spring.ai.ollama.chat.options.model}")
    private String model;

    public AiService(ChatClient.Builder builder, DataService dataService) {
        this.dataService = dataService;
        this.chatClient = builder
                .defaultSystem("""
                    You are a helpful assistant for an e-commerce platform.
                    You have access to real data through tools.
                    When users ask about orders or users, always use the tools 
                    to get real data before answering.
                    Always respond in the same language as the user's message.
                    Be concise, professional, and friendly.
                    """)
                .defaultTools(dataService)
                .build();
    }

    private ChatMemory getOrCreateMemory(String sessionId) {
        return memories.computeIfAbsent(sessionId, k ->
                MessageWindowChatMemory.builder()
                        .chatMemoryRepository(new InMemoryChatMemoryRepository())
                        .maxMessages(10)
                        .build());
    }

    public ChatResponse chat(String message, String sessionId) {
        ChatMemory memory = getOrCreateMemory(sessionId);
        String response = chatClient.prompt()
                .user(message)
                .advisors(MessageChatMemoryAdvisor.builder(memory)
                        .conversationId(sessionId)
                        .build())
                .call()
                .content();
        return new ChatResponse(response, model);
    }

	/*
	 * public Flux<String> chatStream(String message, String sessionId) { ChatMemory
	 * memory = getOrCreateMemory(sessionId); return chatClient.prompt()
	 * .user(message) .advisors(MessageChatMemoryAdvisor.builder(memory)
	 * .conversationId(sessionId) .build()) .stream() .content(); }
	 */
    public Flux<String> chatStream(String message, String sessionId) {
        ChatMemory memory = getOrCreateMemory(sessionId);
        
        // Use non-streaming for tool calls, streaming for simple responses
        try {
            String response = chatClient.prompt()
                    .user(message)
                    .advisors(MessageChatMemoryAdvisor.builder(memory)
                            .conversationId(sessionId)
                            .build())
                    .call()
                    .content();
            
            // Simulate streaming by splitting response
            return Flux.fromArray(response.split("(?<=\\s)"))
                    .map(word -> word);
        } catch (Exception e) {
            return Flux.just("Error: " + e.getMessage());
        }
    }

    public void clearMemory(String sessionId) {
        memories.remove(sessionId);
    }
}