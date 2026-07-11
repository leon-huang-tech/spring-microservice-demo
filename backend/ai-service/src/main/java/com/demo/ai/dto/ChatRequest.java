package com.demo.ai.dto;

import org.apache.commons.lang3.StringUtils;

import com.demo.ai.config.AiConstants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @NotBlank(message = "Message cannot be blank")
        @Size(max = 1000, message = "Message cannot exceed 1000 characters")
        String message,
        @NotBlank(message = "Session ID cannot be blank")
        String sessionId,
        //
        String platform,
        String model,
        Double temperature
) {
  public ChatRequest {
    sessionId = StringUtils.defaultIfEmpty(sessionId, AiConstants.DEFAULT_SESSION_ID);
    model = StringUtils.defaultIfEmpty(model, AiConstants.DEFAULT_MODEL);
    platform = StringUtils.defaultIfEmpty(platform, "ollama");
    if (temperature == null || Double.isNaN(temperature)) {
      temperature = AiConstants.DEFAULT_TEMPERATURE;
    }
  }
}