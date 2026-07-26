package com.demo.ai.service;

import com.demo.ai.config.AiConstants;
import com.demo.ai.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Only for Vector
 */
@Service
public class VectorStoreService {
  private static final Logger log = LoggerFactory.getLogger(VectorStoreService.class);
  private final VectorStore vectorStore;
  private final JdbcTemplate jdbcTemplate;

  public VectorStoreService(VectorStore vectorStore, JdbcTemplate jdbcTemplate) {
    this.vectorStore = vectorStore;
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<String> add(List<Document> documents) {
    List<String> vectorIds = documents.stream().map(Document::getId).collect(Collectors.toList());
    try {
      // Note: if called embedding model itself (Ollama HTTP API) is failure, then the whole transaction will be rolled back and no document will be saved in the database.
      vectorStore.add(documents);
      return vectorIds;
    } catch (Exception e) {
      try {
        // If the vector store fails to add documents, we should clean up any partial additions to avoid inconsistencies.
        vectorStore.delete(vectorIds);
      } catch (Exception ex) {
        log.error("Failed to clean up vectors after partial failure", ex);
      }
      throw new ResourceNotFoundException("Failed to add documents to vector store: " + vectorIds);
    }
  }

  public void delete(List<String> vectorIds) {
    vectorStore.delete(vectorIds);
  }

  public String similaritySearch(String query) {
    List<Document> similar = vectorStore.similaritySearch(
     SearchRequest.builder().query(query).topK(AiConstants.MAX_MEMORY_MESSAGES).build());
    if (similar.isEmpty()) {
      return "No relevant information found in the knowledge base.";
    }
    return similar.stream().map(Document::getText).collect(Collectors.joining("\n\n"));
  }

  /**
   * ASSUMPTION: pgvector table is named "vector_store" (Spring AI default).
   * Update AiConstants.SQL_VECTOR if spring.ai.vectorstore.pgvector.table-name is customized.
   */
  public List<String> getAllVectorIds() {
    return jdbcTemplate.queryForList(AiConstants.SQL_VECTOR, String.class);
  }

}
