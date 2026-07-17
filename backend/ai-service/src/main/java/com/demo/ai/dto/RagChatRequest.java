package com.demo.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record RagChatRequest(
 @NotBlank(message = "Question cannot be blank")
 String question
) {}

