package com.demo.ai.config;

import jakarta.annotation.Nonnull;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.Map;

/**
 * Is a AOP-like interceptor for the chat client. <br>
 */
public class ReReadAdvisor implements BaseAdvisor {
  private static final String DEFAULT_USER_TEXT_ADVICE = """
   {re2_input_query}
   Read the question again: {re2_input_query}
   """;

  @Override
  public ChatClientRequest before(ChatClientRequest chatClientRequest, @Nonnull AdvisorChain advisorChain) {
    String contents = chatClientRequest.prompt().getContents();
    String re2InputQuery = PromptTemplate.builder()
     .template(DEFAULT_USER_TEXT_ADVICE)
     .build()//
     .render(Map.of("re2_input_query", contents));
    return chatClientRequest.mutate()
     .prompt(Prompt.builder().content(re2InputQuery).build())
     .build();
  }

  @Override
  public ChatClientResponse after(@Nonnull ChatClientResponse chatClientResponse, @Nonnull AdvisorChain advisorChain) {
    return chatClientResponse;
  }

  /**
   * The order of the advisor. Lower values have higher priority. <br>
   *
   * @return the order of the advisor.
   */
  @Override
  public int getOrder() {
    return 0;
  }
}
