package com.demo.ai.config;

public final class AiConstants {

  private AiConstants() {
  }


  // Chat memory
  public static final int MAX_MEMORY_MESSAGES = 10;

  // Session
  public static final String DEFAULT_SESSION_ID = "default";

  public static final String MODEL_OLLAMA = "ollama";

  public static final String DEFAULT_MODEL = "llama3.1:latest";
  public static final Double DEFAULT_TEMPERATURE = 0.7;

  // TABLE
  public static final String SQL_VECTOR = "SELECT id FROM vector_store";

}
