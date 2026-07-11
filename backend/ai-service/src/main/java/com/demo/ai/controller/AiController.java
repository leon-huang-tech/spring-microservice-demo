package com.demo.ai.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.demo.ai.config.AiConstants;
import com.demo.ai.dto.ApiResponse;
import com.demo.ai.dto.ChatRequest;
import com.demo.ai.dto.ChatResponse;
import com.demo.ai.service.AiService;
import com.demo.ai.service.BaseAiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI", description = "AI chat endpoints")
public class AiController {

    private final AiService aiService;
    private final BaseAiService baseAiService;

    public AiController(AiService aiService, BaseAiService baseAiService) {
        this.aiService = aiService;
        this.baseAiService = baseAiService;
    }

    /**
     * @param request
     * @return
     */
    @PostMapping("/chat")
    @Operation(summary = "Send a message to AI assistant")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @Valid @RequestBody ChatRequest request) {
        String sessionId = request.sessionId() != null ? 
                request.sessionId() : AiConstants.DEFAULT_SESSION_ID;
        ChatResponse response = aiService.chat(request.message(), sessionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * @param message
     * @param sessionId
     * @return
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream a message to AI assistant")
    public Flux<String> chatStream(
            @RequestParam String message,
            @RequestParam(required = false, defaultValue = AiConstants.DEFAULT_SESSION_ID) String sessionId) {
        return aiService.chatStream(message, sessionId);
    }

    /**
     * @param sessionId
     * @return
     */
    @DeleteMapping("/chat/memory/{sessionId}")
    @Operation(summary = "Clear chat memory for a session")
    public ResponseEntity<ApiResponse<String>> clearMemory(
            @PathVariable String sessionId) {
        aiService.clearMemory(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Memory cleared for session: " + sessionId));
    }

    /**
     * @return
     */
    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("AI Service is running"));
    }
    
    /**
     * curl -N "http://localhost:8083/api/ai/specifyPlatforms?message=wakaka&platform=ollama&model=llama3.1:latest&temperature=0.8"
     * @param message
     * @param options
     * @return
     */
    @GetMapping(value = "/specifyPlatforms", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "test more platforms")
    public Flux<String> specifyPlatforms(
        @RequestParam String message,
        ChatRequest options
        ) {
      return baseAiService.streamChatMorePlatforms(message, options);
    }
    
}