package com.demo.ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class RagService {

  private final VectorStore vectorStore;
  private final ChatClient chatClient;

  public RagService(VectorStore vectorStore, ChatClient.Builder builder) {
    this.vectorStore = vectorStore;
    this.chatClient = builder.build();
  }

  public void loadDocuments(List<String> texts) {
    List<Document> documents = texts.stream()
     .map(Document::new)
     .collect(Collectors.toList());
    vectorStore.add(documents);
  }

  public String askWithContext(String question) {
    List<Document> similarDocuments = vectorStore.similaritySearch(
     SearchRequest.builder()
      .query(question)
      .topK(3)
      .build()
    );

    String context = Objects.requireNonNull(similarDocuments).stream()
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