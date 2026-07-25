package com.demo.ai.service;

import com.demo.ai.dto.ChatRequest;
import com.demo.ai.prompt.AiPrompts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class RagService {
  private static final Logger log = LoggerFactory.getLogger(RagService.class);

  private final KnowledgeService knowledgeService;
  private final ChatModelRegistry modelRegistry;

  public RagService(KnowledgeService knowledgeService, ChatModelRegistry modelRegistry) {
    this.knowledgeService = knowledgeService;
    this.modelRegistry = modelRegistry;
  }

  //
  @Tool(description = AiPrompts.SYSTEM_TOOLS_KNOWLEDGE_RAG)
  public String searchKnowledgeBase(String query) {
    log.info("searchKnowledgeBase tool invoked with query: '{}'", query); // 我喜欢吃吃一中
    String result = knowledgeService.searchKnowledgeBase(query);
    log.info("searchKnowledgeBase result: '{}'", result);
    return result;
  }

  public String askWithContext(ChatRequest chatRequest) {
    String context = knowledgeService.searchKnowledgeBase(chatRequest.message());
    String promptText = AiPrompts.SYSTEM_PROMPT_RAG_AUGMENTED
     .formatted(context, chatRequest.message());

    ChatModel chatModel = modelRegistry.getModel(chatRequest.platform());
    if (chatModel == null) {
      throw new IllegalArgumentException("Unsupported platform: " + chatRequest.platform());
    }

    ChatClient chatClient = ChatClient.builder(chatModel)
     .defaultOptions(ChatOptions.builder()
      .model(chatRequest.model())
      .temperature(chatRequest.temperature()))
     .build();

    return chatClient.prompt()
     .user(promptText)
     .call()
     .content();
  }
}
