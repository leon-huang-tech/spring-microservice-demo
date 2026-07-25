package com.demo.ai.controller;

import com.demo.ai.dto.ApiResponse;
import com.demo.ai.dto.ChatRequest;
import com.demo.ai.dto.ChatResponse;
import com.demo.ai.service.AiService;
import com.demo.ai.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI", description = "AI chat endpoints")
public class AiController {
    private static final Logger log = LoggerFactory.getLogger(AiController.class);

    private final AiService aiService;
    private final RagService ragService;

    public AiController(AiService aiService, RagService ragService) {
        this.aiService = aiService;
        this.ragService = ragService;
    }

    @PostMapping("/chat")
    @Operation(summary = "Send a message to AI assistant")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@Valid @RequestBody ChatRequest chatRequest) {
        ChatResponse response = aiService.chat(chatRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * curl -v -N 'http://localhost:8083/api/ai/chat/stream?message=wakaka&platform=ollama&model=llama3.1:latest&temperature=0.8'
     * the '-N' means to Disable buffering of the output stream, so that the response is sent to the client as soon as it is available.
     * -v print detail status
     * -i print short status
     * NOTE: The url incldes '&', so it should be wrapped in single quotes to avoid shell interpretation issues.
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream a message to AI assistant")
    public Flux<String> chatStream(ChatRequest chatRequest) {
        log.debug("chatRequest.message: '{}'", chatRequest.message());
        return aiService.chatStream(chatRequest);
    }

    @DeleteMapping("/chat/memory/{sessionId}")
    @Operation(summary = "Clear chat memory for a session")
    public ResponseEntity<ApiResponse<String>> clearMemory(@PathVariable String sessionId) {
        aiService.clearMemory(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Memory cleared for session: " + sessionId));
    }

    /**
     * @return for health check
     */
    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("AI Service is running"));
    }
    
    /**
     * RAG
     */
    @PostMapping("/rag/chat")
    @Operation(summary = "Generate a response using Retrieval-Augmented Generation (RAG)")
    public ResponseEntity<ApiResponse<String>> ragChat(
     @Valid @RequestBody ChatRequest chatRequest) {
        String answer = ragService.askWithContext(chatRequest);
        return ResponseEntity.ok(ApiResponse.success(answer));
    }
}