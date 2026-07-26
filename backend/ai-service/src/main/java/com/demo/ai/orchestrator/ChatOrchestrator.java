package com.demo.ai.orchestrator;

import com.demo.ai.dto.ChatProfile;
import com.demo.ai.dto.ChatRequest;
import com.demo.ai.dto.ChatResponse;
import com.demo.ai.service.AiService;
import com.demo.ai.service.ChatClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatOrchestrator {
  private static final Logger log = LoggerFactory.getLogger(ChatOrchestrator.class);

  private final ChatClientFactory chatClientFactory;
  private final AiService aiService;

  public ChatOrchestrator(ChatClientFactory chatClientFactory, AiService aiService) {
    this.chatClientFactory = chatClientFactory;
    this.aiService = aiService;
  }

  /*
   * Note:
   * call() is synchronous, so try/catch can catch exceptions directly.
   * stream() is reactive, where exceptions occur after Flux subscription,
   * so onErrorResume must be used instead of try/catch (try/catch cannot catch exceptions inside the async pipeline).
   */
  public ChatResponse chat(ChatRequest chatRequest) {
    ChatMemory memory = aiService.getOrCreateMemory(chatRequest.sessionId());
    try {
      ChatClient chatClient = chatClientFactory.create(chatRequest, ChatProfile.CHAT_RAG);
      String response = chatClient.prompt()
       .user(chatRequest.message())
       .advisors(MessageChatMemoryAdvisor.builder(memory).build())
       .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatRequest.sessionId()))
       .call()
       .content();
      return new ChatResponse(response, aiService.getModel());
    } catch (Exception e) {
      log.error("AI service error: {}", e.getMessage(), e);
      String userMessage;
      if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
        userMessage = "AI service is currently unavailable. Please try again later.";
      } else {
        userMessage = "Error: " + e.getMessage();
      }
      return new ChatResponse(userMessage, aiService.getModel());
    }
  }

  /*
   * Note:
   * call() is synchronous, so try/catch can catch exceptions directly.
   * stream() is reactive, where exceptions occur after Flux subscription,
   * so onErrorResume must be used instead of try/catch (try/catch cannot catch exceptions inside the async pipeline).
   */
  public Flux<String> chatStream(ChatRequest chatRequest) {
    ChatMemory memory = aiService.getOrCreateMemory(chatRequest.sessionId());
    ChatClient chatClient = chatClientFactory.create(chatRequest, ChatProfile.CHAT_RAG);
    return chatClient.prompt()
     .user(chatRequest.message())
     .advisors(MessageChatMemoryAdvisor.builder(memory).build())
     .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatRequest.sessionId()))
     .stream()
     .content()
     .doOnNext(token -> log.debug("chunk: '{}'", token))
     // (Fix 'preserve whitespace' )serializes each chunk via ObjectMapper before emitting
     .map(aiService::toJson)
     .onErrorResume(e -> {
       log.error("AI service stream error: {}", e.getMessage(), e);
       String userMessage =
        (e.getMessage() != null && e.getMessage().contains("Connection refused"))
         ? "AI service is currently unavailable. Please try again later."
         : "Error: " + e.getMessage();
       return Flux.just(aiService.toJson(userMessage));
     });
  }

}
