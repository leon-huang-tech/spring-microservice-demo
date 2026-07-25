package com.demo.ai.service;

import com.demo.ai.dto.ChatProfile;
import com.demo.ai.dto.ChatRequest;
import com.demo.ai.prompt.AiPrompts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ChatClientFactory {
  private static final Logger log = LoggerFactory.getLogger(ChatClientFactory.class);
  private final ChatModelRegistry modelRegistry;
  private final DataService dataService;
  private final RagService ragService;

  @Value("${spring.ai.default.platform}")
  private String defaultPlatform;

  @Value("${spring.ai.default.model}")
  private String defaultModel;

  @Value("${spring.ai.default.temperature:0.7}")
  private double defaultTemperature;

  public ChatClientFactory(ChatModelRegistry modelRegistry, DataService dataService, RagService ragService) {
      this.modelRegistry = modelRegistry;
      this.dataService = dataService;
      this.ragService = ragService;
  }

  /**
   * @return specify more platforms, such as ollama, openai, etc.
   */
  public ChatClient create(ChatRequest request, ChatProfile profile) {
    ChatModel model = modelRegistry.getModel(request.platform());
    if (model == null) {
      throw new IllegalArgumentException("Unsupported platform: " + request.platform());
    }

    ChatClient.Builder builder = ChatClient.builder(model)
     .defaultOptions(ChatOptions.builder()
      .model(request.model())
      .temperature(request.temperature()));

    switch (profile) {
      case CHAT_RAG -> {
        builder
         .defaultSystem(AiPrompts.SYSTEM_PROMPT_ECO)
         .defaultTools(dataService, ragService);
      }
    }
    return builder.build();
  }
}
