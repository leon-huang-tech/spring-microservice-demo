package com.demo.ai.dto;

public record ChatResponse(
        String message,
        String model
) {}