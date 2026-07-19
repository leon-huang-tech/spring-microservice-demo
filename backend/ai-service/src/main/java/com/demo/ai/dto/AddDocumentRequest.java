package com.demo.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record AddDocumentRequest(
 @NotBlank(message = "Content cannot be blank") String content
) {}
