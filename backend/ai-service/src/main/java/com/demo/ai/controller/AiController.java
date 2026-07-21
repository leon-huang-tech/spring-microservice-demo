package com.demo.ai.controller;

import com.demo.ai.config.AiConstants;
import com.demo.ai.dto.ApiResponse;
import com.demo.ai.dto.ChatRequest;
import com.demo.ai.dto.ChatResponse;
import com.demo.ai.dto.RagChatRequest;
import com.demo.ai.service.AiService;
import com.demo.ai.service.BaseAiService;
import com.demo.ai.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI", description = "AI chat endpoints")
public class AiController {

    private final AiService aiService;
    private final BaseAiService baseAiService;
    private final RagService ragService;

    public AiController(AiService aiService, BaseAiService baseAiService, RagService ragService) {
        this.aiService = aiService;
        this.baseAiService = baseAiService;
        this.ragService = ragService;
    }

    @PostMapping("/chat")
    @Operation(summary = "Send a message to AI assistant")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @Valid @RequestBody ChatRequest request) {
        String sessionId = request.sessionId() != null ? 
                request.sessionId() : AiConstants.DEFAULT_SESSION_ID;
        ChatResponse response = aiService.chat(request.message(), sessionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream a message to AI assistant")
    public Flux<String> chatStream(
            @RequestParam String message,
            @RequestParam(required = false, defaultValue = AiConstants.DEFAULT_SESSION_ID) String sessionId) {
        return aiService.chatStream(message, sessionId);
    }

    @DeleteMapping("/chat/memory/{sessionId}")
    @Operation(summary = "Clear chat memory for a session")
    public ResponseEntity<ApiResponse<String>> clearMemory(
            @PathVariable String sessionId) {
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
     * curl -N {@code "http://localhost:8083/api/ai/specifyPlatforms?message=wakaka&platform=ollama&model=llama3.1:latest&temperature=0.8"}
     * the '-N' means to Disable buffering of the output stream, so that the response is sent to the client as soon as it is available.
     * NOTE: The url incldes '&', so it should be wrapped in single quotes to avoid shell interpretation issues.
     * @return specify more platforms, such as ollama, openai, etc.
     */
    @GetMapping(value = "/specifyPlatforms", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "test more platforms")
    public Flux<String> specifyPlatforms(
        ChatRequest options
        ) {
      return baseAiService.streamChatMorePlatforms(options);
    }


    /////////////////
    @PostMapping("/rag/load")
    @Operation(summary = "Load sample documents into vector store")
    public ResponseEntity<ApiResponse<String>> loadDocuments() {
        ragService.loadDocuments(List.of(
         "Acme Corp is a fintech company that provides fast and low-cost international money transfers using the real mid-market exchange rate.",
         "Our e-commerce platform supports order tracking, user account management, and AI-powered customer assistance.",
         "Orders can have the following statuses: PENDING, PROCESSING, and COMPLETED. Customers can view their order history at any time.",
         "For refund requests, customers should contact support within 30 days of purchase. Refunds are processed within 5-7 business days.",
         "The platform uses JWT-based authentication. Users must log in with their email and password to access protected resources."
        ));
        return ResponseEntity.ok(ApiResponse.success("Documents loaded into vector store"));
    }

    @PostMapping("/rag/chat")
    @Operation(summary = "Ask a question using RAG (Retrieval-Augmented Generation)")
    public ResponseEntity<ApiResponse<String>> ragChat(
     @Valid @RequestBody RagChatRequest request) {
        String answer = ragService.askWithContext(request.question());
        return ResponseEntity.ok(ApiResponse.success(answer));
    }
}