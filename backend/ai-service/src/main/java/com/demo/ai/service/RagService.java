package com.demo.ai.service;

import com.demo.ai.exception.ResourceNotFoundException;
import com.demo.ai.model.KnowledgeDocument;
import com.demo.ai.repository.KnowledgeDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {

  private static final Logger log = LoggerFactory.getLogger(RagService.class);

  private final VectorStore vectorStore;
  private final ChatClient chatClient;
  private final KnowledgeDocumentRepository knowledgeDocumentRepository;

  public RagService(VectorStore vectorStore, ChatClient.Builder builder,
                    KnowledgeDocumentRepository knowledgeDocumentRepository) {
    this.vectorStore = vectorStore;
    this.chatClient = builder.build();
    this.knowledgeDocumentRepository = knowledgeDocumentRepository;
  }

//  @Transactional
  public void loadDocuments(List<String> texts) {
    List<Document> documents = texts.stream()
     .map(Document::new)
     .collect(Collectors.toList());
    List<String> docIds = documents.stream().map(Document::getId).collect(Collectors.toList());

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
      throw new ResourceNotFoundException("Failed to add documents to vector store" + docIds);
    }

    //knowledgeDocumentRepository.saveAll(records);
    this.saveMetadataRecords(texts, documents);
  }

  @Transactional
  public void saveMetadataRecords(List<String> texts, List<Document> documents) {
    List<KnowledgeDocument> records = new java.util.ArrayList<>();
    for (int i = 0; i < texts.size(); i++) {
      records.add(new KnowledgeDocument(texts.get(i), documents.get(i).getId()));
    }
    knowledgeDocumentRepository.saveAll(records);
  }


  @Transactional
  public void deleteDocument(Long id) {
    KnowledgeDocument record = knowledgeDocumentRepository.findById(id)
     .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));

    vectorStore.delete(List.of(record.getVectorStoreId()));
    knowledgeDocumentRepository.deleteById(id);
  }

  public String askWithContext(String question) {
    List<Document> similarDocuments = vectorStore.similaritySearch(
     SearchRequest.builder()
      .query(question)
      .topK(3)
      .build()
    );

    String context = similarDocuments.stream()
     .map(Document::getText)
     .collect(Collectors.joining("\n\n"));

    String promptText = """
                Answer the question based only on the context below.
                If the context does not contain the answer, say you don't know.

                Context:
                %s

                Question: %s
                """.formatted(context, question);

    return chatClient.prompt()
     .user(promptText)
     .call()
     .content();
  }
}