package com.demo.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component("ollama")
public class OllamaHealthIndicator implements HealthIndicator {

  private final String ollamaBaseUrl;
  private final RestClient restClient;

  public OllamaHealthIndicator(
   @Value("${spring.ai.ollama.base-url:http://localhost:11434}") String ollamaBaseUrl) {
    this.ollamaBaseUrl = ollamaBaseUrl;

    SimpleClientHttpRequestFactory reqFactory = new SimpleClientHttpRequestFactory();
    reqFactory.setConnectTimeout(1000);
    reqFactory.setReadTimeout(1000);

    this.restClient = RestClient.builder()
     .requestFactory(reqFactory)
     .baseUrl(ollamaBaseUrl)
     .build();
  }

  @Override
  public Health health() {
    try {
      Map<?, ?> response = restClient.get()
       //.uri("/api/version")
       .retrieve()
       .body(Map.class);

      return Health.up()
       .withDetail("url", ollamaBaseUrl)
       //.withDetail("version", response != null ? response.get("version") : "unknown")
       .build();
    } catch (Exception e) {
      return Health.down(e)
       .withDetail("url", ollamaBaseUrl)
       .withDetail("error", e.getMessage())
       .build();
    }
  }
}