package com.demo.ai.controller;

import com.demo.ai.dto.AddDocumentRequest;
import com.demo.ai.dto.ApiResponse;
import com.demo.ai.dto.OrphanCheckResponse;
import com.demo.ai.model.KnowledgeDocument;
import com.demo.ai.repository.KnowledgeDocumentRepository;
import com.demo.ai.service.KnowledgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai/rag/documents")
@Tag(name = "Knowledge Base", description = "Manage RAG knowledge base documents")
public class KnowledgeController {

  private final KnowledgeService knowledgeService;
  private final KnowledgeDocumentRepository knowledgeDocumentRepository;

  public KnowledgeController(KnowledgeService knowledgeService,
                             KnowledgeDocumentRepository knowledgeDocumentRepository) {
    this.knowledgeService = knowledgeService;
    this.knowledgeDocumentRepository = knowledgeDocumentRepository;
  }

  /**
   * curl -X POST 'http://localhost:8080/api/ai/rag/documents' \
   * -H "Content-Type: application/json" \
   * -d '{"content": "Your document content here"}'
   * -v print detail status
   * -i print short status
   * NOTE: The url incldes '&', so it should be wrapped in single quotes to avoid shell interpretation issues.
   * <br>
   * Description: Add a new document to the knowledge base.
   * Request Body: JSON object containing the document content.
   * Response: JSON object indicating success or failure.
   */
  @PostMapping
  @Operation(summary = "Add a document to the knowledge base")
  public ResponseEntity<ApiResponse<String>> addDocument(
   @Valid @RequestBody AddDocumentRequest request) {
    knowledgeService.addDocuments(List.of(request.content()));
    return ResponseEntity.ok(ApiResponse.success("Document added"));
  }

  /**
   * curl 'http://localhost:8080/api/ai/rag/documents?page=0&size=10'
   * -v print detail status
   * -i print short status
   * NOTE: The url incldes '&', so it should be wrapped in single quotes to avoid shell interpretation issues.
   * <br>
   * Description: List knowledge base documents with pagination.
   * Request Parameters:
   * - page: The page number (default is 0).
   * - size: The number of documents per page (default is 10).
   * Response: JSON object containing a paginated list of knowledge base documents.
   *
   */
  @GetMapping
  @Operation(summary = "List knowledge base documents with pagination")
  public ResponseEntity<ApiResponse<Page<KnowledgeDocument>>> listDocuments(
   @RequestParam(name = "page", defaultValue = "0") int page,
   @RequestParam(name = "size", defaultValue = "10") int size) {
    Page<KnowledgeDocument> result = knowledgeDocumentRepository.findAll(
     PageRequest.of(page, size, Sort.by("id").descending()));
    return ResponseEntity.ok(ApiResponse.success(result));
  }

  /**
   * curl -X DELETE 'http://localhost:8080/api/ai/rag/documents/1'
   * -v print detail status
   * -i print short status
   * NOTE: The url incldes '&', so it should be wrapped in single quotes to avoid shell interpretation issues.
   * <br>
   * Description: Delete a document from the knowledge base.
   * Path Variable:
   * - id: The ID of the document to delete.
   * Response: JSON object indicating success or failure.
   *
   */
  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a document from the knowledge base")
  public ResponseEntity<ApiResponse<String>> deleteDocument(@PathVariable(name = "id") Long id) {
    knowledgeService.deleteDocument(id);
    return ResponseEntity.ok(ApiResponse.success("Document deleted"));
  }

  /**
   * curl 'http://localhost:8080/api/ai/rag/documents/orphans'
   * -v print detail status
   * -i print short status
   * NOTE: The url incldes '&', so it should be wrapped in single quotes to avoid shell interpretation issues.
   * <br>
   * Description: Check for orphaned records between the vector store and
   * the knowledge_documents metadata table (maintenance/admin endpoint).
   *
   * @return lists of orphaned vectors and orphaned metadata rows
   */
  @GetMapping("/orphans")
  @Operation(summary = "Check for orphaned records between vector store and metadata table")
  public ResponseEntity<ApiResponse<OrphanCheckResponse>> checkOrphans() {
    OrphanCheckResponse result = knowledgeService.findOrphanedRecords();
    return ResponseEntity.ok(ApiResponse.success(result));
  }

  /**
   * curl -X DELETE 'http://localhost:8080/api/ai/rag/documents/orphans/vectors/{vectorId}'
   * -v print detail status
   * -i print short status
   * NOTE: The url incldes '&', so it should be wrapped in single quotes to avoid shell interpretation issues.
   * <br>
   * Description: Delete an orphaned vector (vector store entry with no
   * matching knowledge_documents row).
   */
  @DeleteMapping("/orphans/vectors/{vectorId}")
  @Operation(summary = "Delete an orphaned vector store entry")
  public ResponseEntity<ApiResponse<String>> deleteOrphanedVector(
   @PathVariable String vectorId) {
    knowledgeService.deleteOrphanedVector(vectorId);
    return ResponseEntity.ok(ApiResponse.success("Orphaned vector deleted"));
  }

  /**
   * curl -X DELETE 'http://localhost:8080/api/ai/rag/documents/orphans/metadata/{id}'
   * -v print detail status
   * -i print short status
   * NOTE: The url incldes '&', so it should be wrapped in single quotes to avoid shell interpretation issues.
   * <br>
   * Description: Delete an orphaned metadata row (knowledge_documents row
   * whose vector no longer exists in the vector store).
   */
  @DeleteMapping("/orphans/metadata/{id}")
  @Operation(summary = "Delete an orphaned metadata row")
  public ResponseEntity<ApiResponse<String>> deleteOrphanedMetadata(
   @PathVariable Long id) {
    knowledgeService.deleteOrphanedMetadata(id);
    return ResponseEntity.ok(ApiResponse.success("Orphaned metadata deleted"));
  }
}