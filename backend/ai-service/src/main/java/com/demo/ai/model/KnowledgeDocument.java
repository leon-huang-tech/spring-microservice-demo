package com.demo.ai.model;

import jakarta.persistence.*;
import java.time.Instant;
@Entity
@Table(name = "knowledge_documents")
public class KnowledgeDocument {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(name = "vector_store_id")
  private String vectorStoreId;

  @Column(nullable = false)
  private Instant createdAt;

  public KnowledgeDocument() {}

  public KnowledgeDocument(String content, String vectorStoreId) {
    this.content = content;
    this.vectorStoreId = vectorStoreId;
    this.createdAt = Instant.now();
  }

  public Long getId() { return id; }
  public String getContent() { return content; }
  public String getVectorStoreId() { return vectorStoreId; }
  public Instant getCreatedAt() { return createdAt; }
}
