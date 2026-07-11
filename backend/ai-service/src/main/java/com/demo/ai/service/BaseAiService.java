package com.demo.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.demo.ai.dto.ChatRequest;

import reactor.core.publisher.Flux;

@Service
public class BaseAiService {
  private static final Logger log = LoggerFactory.getLogger(BaseAiService.class);
  private final ChatModelRegistry modelRegistry;

  @Value("${spring.ai.ollama.chat.options.model}")
  private String model;

  public BaseAiService(ChatModelRegistry modelRegistry) {
      this.modelRegistry = modelRegistry;
  }
  
  public Flux<String> streamChatMorePlatforms(ChatRequest options) {
    String platform = options.platform();
    ChatModel chatModel = modelRegistry.getModel(platform);
    
    if (chatModel == null) {
        return Flux.just("Unsupported platform: " + platform);
    }

    ChatClient dynamicClient = ChatClient.builder(chatModel)
        .defaultOptions(ChatOptions.builder()
            .temperature((double) (options.temperature() != null ? options.temperature().floatValue() : 0.7f))
            .model(options.model() != null ? options.model() : model)
            .build())
        .build();

    return dynamicClient.prompt()
        .user(options.message())
        .stream()
        .content()
        .doOnNext(token -> log.debug("Stream token: {}", token));
  }

}
