package com.demo.ai.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class ChatModelRegistry {
  private final Map<String, ChatModel> registry = new ConcurrentHashMap<>();
  private final OllamaChatModel ollamaChatModel;

  public ChatModelRegistry(OllamaChatModel ollamaChatModel) {
    this.ollamaChatModel = ollamaChatModel;
  }

  @PostConstruct
  public void init() {
    registry.put("ollama", ollamaChatModel);
  }

  public ChatModel getModel(String platform) {
    return registry.get(platform);
  }
}