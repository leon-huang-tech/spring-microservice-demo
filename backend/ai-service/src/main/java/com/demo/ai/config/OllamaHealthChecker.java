package com.demo.ai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OllamaHealthChecker implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(OllamaHealthChecker.class);

  @Value("${spring.ai.ollama.base-url:http://localhost:11434}")
  private String ollamaBaseUrl;

  @Override
  public void run(String... args) {
    try {
      RestClient restClient = RestClient.create();
      String response = restClient.get()
       .uri(ollamaBaseUrl)
       .retrieve()
       .body(String.class);

      log.info("Ollama Service is Running. Response: {}", response);
    } catch (Exception e) {
      log.error("=============================================================");
      log.error("Warning: Cannot connect to Ollama service ({})！", ollamaBaseUrl);
      log.error("Please confirm that Ollama is running locally (run command: ollama serve), otherwise the AI interface will not function properly.");
      log.error("=============================================================");
      // throw new IllegalStateException("Ollama service is not running", e);
    }
  }
}