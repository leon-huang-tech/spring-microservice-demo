package com.demo.ai.controller;

import com.demo.ai.dto.ApiResponse;
import com.demo.ai.dto.ChatRequest;
import com.demo.ai.dto.ChatResponse;
import com.demo.ai.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI", description = "AI chat endpoints")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    @Operation(summary = "Send a message to AI assistant")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @Valid @RequestBody ChatRequest request) {
        String sessionId = request.sessionId() != null ? 
                request.sessionId() : "default";
        ChatResponse response = aiService.chat(request.message(), sessionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream a message to AI assistant")
    public Flux<String> chatStream(
            @RequestParam String message,
            @RequestParam(required = false, defaultValue = "default") String sessionId) {
        return aiService.chatStream(message, sessionId);
    }

    @DeleteMapping("/chat/memory/{sessionId}")
    @Operation(summary = "Clear chat memory for a session")
    public ResponseEntity<ApiResponse<String>> clearMemory(
            @PathVariable String sessionId) {
        aiService.clearMemory(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Memory cleared for session: " + sessionId));
    }

    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("AI Service is running"));
    }
}