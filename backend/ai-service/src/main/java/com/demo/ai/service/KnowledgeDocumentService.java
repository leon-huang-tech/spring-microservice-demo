package com.demo.ai.service;

import com.demo.ai.exception.ResourceNotFoundException;
import com.demo.ai.model.KnowledgeDocument;
import com.demo.ai.repository.KnowledgeDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class KnowledgeDocumentService {
  private final KnowledgeDocumentRepository knowledgeDocumentRepository;

  public KnowledgeDocumentService(KnowledgeDocumentRepository knowledgeDocumentRepository) {
    this.knowledgeDocumentRepository = knowledgeDocumentRepository;
  }

  @Transactional
  public void saveAll(List<String> texts, List<String> vectorIds) {
    List<KnowledgeDocument> records = new ArrayList<>();
    for (int i = 0; i < texts.size(); i++) {
      records.add(new KnowledgeDocument(texts.get(i), vectorIds.get(i)));
    }
    knowledgeDocumentRepository.saveAll(records);
  }

  public KnowledgeDocument findByIdOrThrow(Long id) {
    return knowledgeDocumentRepository.findById(id)
     .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));
  }

  public void deleteById(Long id) {
    knowledgeDocumentRepository.deleteById(id);
  }

  public List<KnowledgeDocument> findAll() {
    return knowledgeDocumentRepository.findAll();
  }
}
