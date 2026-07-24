package com.demo.ai.repository;

import com.demo.ai.model.KnowledgeDocument;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {
  @Override
  @NullMarked
  Page<KnowledgeDocument> findAll(Pageable pageable);
}
