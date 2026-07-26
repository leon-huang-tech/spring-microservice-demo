package com.demo.ai.service;

import com.demo.ai.dto.OrphanCheckResponse;
import com.demo.ai.model.KnowledgeDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class KnowledgeService {
  private static final Logger log = LoggerFactory.getLogger(KnowledgeService.class);

  private final VectorStoreService vectorStoreService;
  private final KnowledgeDocumentService knowledgeDocumentService;

  public KnowledgeService(VectorStoreService vectorStoreService, KnowledgeDocumentService knowledgeDocumentService) {
    this.vectorStoreService = vectorStoreService;
    this.knowledgeDocumentService = knowledgeDocumentService;
  }

  //  @Transactional
  public void addDocuments(List<String> texts) {
    List<Document> documents = texts.stream().map(Document::new).collect(Collectors.toList());
    List<String> vectorIds = vectorStoreService.add(documents);
    // Note: if called embedding model itself (Ollama HTTP API) is failure, then the whole transaction will be rolled back and no document will be saved in the database.
    try {
      knowledgeDocumentService.saveAll(texts, vectorIds);
    } catch (Exception e) {
      vectorStoreService.delete(vectorIds);
      throw e;
    }
  }

//  @Transactional
  public void deleteDocument(Long id) {
    KnowledgeDocument record = knowledgeDocumentService.findByIdOrThrow(id);
    vectorStoreService.delete(List.of(record.getVectorStoreId()));
    knowledgeDocumentService.deleteById(id);
  }

  // Used by RagService as @Tool
  public String retrieveContext(String query) {
    return vectorStoreService.similaritySearch(query);
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
    List<String> allVectorIds = vectorStoreService.getAllVectorIds();
    List<KnowledgeDocument> allMetadata = knowledgeDocumentService.findAll();

    Set<String> metadataVectorIds = allMetadata.stream()
     .map(KnowledgeDocument::getVectorStoreId).collect(Collectors.toSet());
    List<String> vectorsWithoutMetadata = allVectorIds.stream()
     .filter(id -> !metadataVectorIds.contains(id)).collect(Collectors.toList());

    Set<String> vectorIdSet = new HashSet<>(allVectorIds);
    List<KnowledgeDocument> metadataWithoutVector = allMetadata.stream()
     .filter(doc -> !vectorIdSet.contains(doc.getVectorStoreId())).collect(Collectors.toList());

    return new OrphanCheckResponse(vectorsWithoutMetadata, metadataWithoutVector);
  }

  /**
   * Deletes an orphaned vector (exists in the vector store, but has no
   * matching row in knowledge_documents). No metadata row to clean up —
   * vectorStore.delete is the only operation needed.
   */
  public void deleteOrphanedVector(String vectorId) {
    vectorStoreService.delete(List.of(vectorId));
  }

  /**
   * Deletes an orphaned metadata row (exists in knowledge_documents, but
   * the vector it points to no longer exists in the vector store).
   * Only removes the metadata row — there's no vector left to delete.
   */
  public void deleteOrphanedMetadata(Long id) {
    knowledgeDocumentService.deleteById(id);
  }
}