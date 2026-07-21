package com.demo.ai.service;

import com.demo.ai.config.AiConstants;
import com.demo.ai.dto.ChatRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class BaseAiService {
  private static final Logger log = LoggerFactory.getLogger(BaseAiService.class);
  private final ChatModelRegistry modelRegistry;

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
            .temperature(options.temperature())
            .model(options.model())
            /*.build()*/)
        .defaultSystem(AiConstants.SYSTEM_PROMPT)
        .build();

    return dynamicClient.prompt()
        //.system(p -> p.param("name", "leon"))//
        .user(options.message())
        .stream()
        .content()
        .doOnNext(token -> log.debug("Stream token: {}", token));
  }

}
