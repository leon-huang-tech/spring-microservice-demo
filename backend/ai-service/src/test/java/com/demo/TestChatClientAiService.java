package com.demo;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.demo.ai.AiServiceApplication;

import reactor.core.publisher.Flux;

@SpringBootTest(classes = AiServiceApplication.class, properties = { "eureka.client.enabled=false", // disable Eureka
                                                                                                    // client.
    "spring.cloud.discovery.enabled=false" // disable discovery services.
})
public class TestChatClientAiService {

  @Autowired
  private ChatClient.Builder chatclientBuilder;

  /**
   * test the chat client basic functionality. <br>
   */
  @Test
  public void TestChatClientBasic() {
    ChatClient chatClient = chatclientBuilder.build();
    String content = chatClient.prompt()//
        .user("test basic")//
        .call()//
        .content();
    System.out.println(LogUtils.getLogPrefix2(this.getClass()));
    System.out.println(content);
    System.out.println("-----------------------");
  }

  /**
   * test the chat client stream method, which returns a Flux<String> of the content. <br>
   */
  @Test
  public void TestChatClientStream() {
    ChatClient chatClient = chatclientBuilder.build();
    Flux<String> content = chatClient.prompt()//
        .user("test stream")//
        .stream()//
        .content();
    System.out.println(LogUtils.getLogPrefix2(this.getClass()));
    content.toIterable().forEach(System.out::println);
    System.out.println("-----------------------");
  }

  /**
   * test the chat client with a specific OllamaChatModel bean injected. <br>
   * 
   * @param ollamaChatModel
   */
  @Test
  public void TestSpecifyModel(@Autowired OllamaChatModel ollamaChatModel) {
    ChatClient chatClient = ChatClient.builder(ollamaChatModel).build();
    Flux<String> content = chatClient.prompt()//
        .user("test stream")//
        .stream()//
        .content();
    System.out.println(LogUtils.getLogPrefix2(this.getClass()));
    content.toIterable().forEach(System.out::println);
    System.out.println("-----------------------");
  }
}
