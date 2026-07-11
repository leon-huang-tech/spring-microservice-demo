package com.demo;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.demo.ai.AiServiceApplication;

import reactor.core.publisher.Flux;

@SpringBootTest(classes = AiServiceApplication.class, properties = {"eureka.client.enabled=false", // disable Eureka
 "spring.cloud.discovery.enabled=false" // disable discovery services.
})

public class TestOllamaChatModelAiService {
  /**
   * new OllamaChatModel() is not recommended because it does not use Spring's
   * dependency injection. Instead, the OllamaChatModel bean should be injected
   * into the test class using the @Autowired annotation. This allows for better
   * management of the bean's lifecycle and configuration. <br>
   */
  @Autowired
  private OllamaChatModel chatModel;

  /**
   * test the OllamaChatModel basic functionality. <br>
   */
  @Test
  public void TestOllama() {
    String content = chatModel.call("this is a basic test");
    System.out.println(LogUtils.getLogPrefix2(this.getClass()));
    System.out.println(content);
    System.out.println("-----------------------");
  }

  /**
   * test the OllamaChatModel stream method, which returns a Flux<String> of the
   * content. <br>
   */
  @Test
  public void TestOllamaStream() {
    Flux<String> content = chatModel.stream("this is a stream test");
    //
    System.out.println(LogUtils.getLogPrefix2(this.getClass()));
    content.toIterable().forEach(System.out::println);
    System.out.println("-----------------------");
  }

  /**
   * test the OllamaChatModel with OllamaOptions. <br>
   */
  @Test
  public void TestOllamaOptions() {
    /**
     * The temperature of the model. Increasing the temperature will make the model
     * answer more creatively. (Default: 0.8)
     */
    OllamaOptions options = OllamaOptions.builder()
     // .model()
     // .temperature(1.9d)
     .build();
    Prompt prompt = new Prompt("this is an options test", options);
    ChatResponse res = chatModel.call(prompt);
    AssistantMessage assistantMessage = res.getResult().getOutput();
    //
    System.out.println(LogUtils.getLogPrefix2(this.getClass()));
    System.out.println(assistantMessage.getText());
    System.out.println("-----------------------");

  }

  /**
   * test the OllamaChatModel with OllamaOptions and UserMessage. <br>
   */
  @Test
  public void TestOllamaOptions2() {
    OllamaOptions options = OllamaOptions.builder().build();
    UserMessage userMessage = UserMessage.builder().text("this is an options test2")
//        .media(null) // Need Multimodality support
     .build();
    Prompt prompt = new Prompt(userMessage, options);
    ChatResponse res = chatModel.call(prompt);
    AssistantMessage assistantMessage = res.getResult().getOutput();
    //
    System.out.println(LogUtils.getLogPrefix2(this.getClass()));
    System.out.println(assistantMessage.getText());
    System.out.println("-----------------------");

  }
}
