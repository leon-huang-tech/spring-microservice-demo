package com.demo.ai.service;

import com.demo.ai.config.AiConstants;
import com.demo.ai.dto.OrphanCheckResponse;
import com.demo.ai.exception.ResourceNotFoundException;
import com.demo.ai.model.KnowledgeDocument;
import com.demo.ai.repository.KnowledgeDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class KnowledgeService {
  private static final Logger log = LoggerFactory.getLogger(KnowledgeService.class);

  private final VectorStore vectorStore;
  private final KnowledgeDocumentRepository knowledgeDocumentRepository;
  private final JdbcTemplate jdbcTemplate;

  public KnowledgeService(VectorStore vectorStore,
                          KnowledgeDocumentRepository knowledgeDocumentRepository,
                          JdbcTemplate jdbcTemplate) {
    this.vectorStore = vectorStore;
    this.knowledgeDocumentRepository = knowledgeDocumentRepository;
    this.jdbcTemplate = jdbcTemplate;
  }

  //  @Transactional
  public void addDocuments(List<String> texts) {
    List<Document> documents = texts.stream()
     .map(Document::new)
     .collect(Collectors.toList());
    List<String> docIds = documents.stream()
     .map(Document::getId)
     .collect(Collectors.toList());
    // Note: if called embedding model itself (Ollama HTTP API) is failure, then the whole transaction will be rolled back and no document will be saved in the database.
    try {
      vectorStore.add(documents);
    } catch (Exception e) {
      // If the vector store fails to add documents, we should clean up any partial additions to avoid inconsistencies.
      try {
        vectorStore.delete(docIds);
      } catch (Exception ex) {
        log.error("Failed to clean up vectors after partial failure", ex);
      }
      throw new ResourceNotFoundException(
       "Failed to add documents to vector store: " + docIds);
    }
    saveMetadataRecords(texts, documents);
  }

  @Transactional
  protected void saveMetadataRecords(List<String> texts, List<Document> documents) {
    List<KnowledgeDocument> records = new java.util.ArrayList<>();
    for (int i = 0; i < texts.size(); i++) {
      records.add(new KnowledgeDocument(
       texts.get(i), documents.get(i).getId()));
    }
    knowledgeDocumentRepository.saveAll(records);
  }

  @Transactional
  public void deleteDocument(Long id) {
    KnowledgeDocument record = knowledgeDocumentRepository.findById(id)
     .orElseThrow(() ->
      new ResourceNotFoundException("Document not found: " + id));
    vectorStore.delete(List.of(record.getVectorStoreId()));
    knowledgeDocumentRepository.deleteById(id);
  }

  // Used by RagService as @Tool
  public String searchKnowledgeBase(String query) {
    List<Document> similar = vectorStore.similaritySearch(
     SearchRequest.builder()
      .query(query)
      .topK(AiConstants.MAX_MEMORY_MESSAGES)
      .build());
    if (similar.isEmpty()) {
      return "No relevant information found in the knowledge base.";
    }
    return similar.stream()
     .map(Document::getText)
     .collect(Collectors.joining("\n\n"));
  }

  /**
   * Compares the pgvector table against the knowledge_documents metadata
   * table and reports orphans in both directions.
   * <br>
   * ASSUMPTION: the pgvector table is named "vector_store" (Spring AI's
   * default). If spring.ai.vectorstore.pgvector.table-name is customized,
   * update the SQL below to match.
   */
  public OrphanCheckResponse findOrphanedRecords() {
    // the pgvector table is named "vector_store" (Spring AI's default)
    List<String> allVectorIds = jdbcTemplate.queryForList(
     AiConstants.SQL_VECTOR, String.class);
    List<KnowledgeDocument> allMetadata =
     knowledgeDocumentRepository.findAll();

    Set<String> metadataVectorIds = allMetadata.stream()
     .map(KnowledgeDocument::getVectorStoreId)
     .collect(Collectors.toSet());

    List<String> vectorsWithoutMetadata = allVectorIds.stream()
     .filter(id -> !metadataVectorIds.contains(id))
     .collect(Collectors.toList());

    Set<String> vectorIdSet = new HashSet<>(allVectorIds);
    List<KnowledgeDocument> metadataWithoutVector = allMetadata.stream()
     .filter(doc -> !vectorIdSet.contains(doc.getVectorStoreId()))
     .collect(Collectors.toList());

    return new OrphanCheckResponse(vectorsWithoutMetadata, metadataWithoutVector);
  }

  /**
   * Deletes an orphaned vector (exists in the vector store, but has no
   * matching row in knowledge_documents). No metadata row to clean up —
   * vectorStore.delete is the only operation needed.
   */
  public void deleteOrphanedVector(String vectorId) {
    vectorStore.delete(List.of(vectorId));
  }

  /**
   * Deletes an orphaned metadata row (exists in knowledge_documents, but
   * the vector it points to no longer exists in the vector store).
   * Only removes the metadata row — there's no vector left to delete.
   */
  public void deleteOrphanedMetadata(Long id) {
    knowledgeDocumentRepository.deleteById(id);
  }
}