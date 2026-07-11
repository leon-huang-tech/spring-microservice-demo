package com.demo;

import com.demo.ai.AiServiceApplication;
import com.demo.ai.config.AiConstants;
import com.demo.ai.config.ReReadAdvisor;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;

@SpringBootTest(classes = AiServiceApplication.class, properties = {"eureka.client.enabled=false", // disable Eureka
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
     .user("hello")//
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
  public void TestStream() {
    ChatClient chatClient = chatclientBuilder.build();
    Flux<String> content = chatClient.prompt()//
     .user("hello")//
     .stream()//
     .content();
    System.out.println(LogUtils.getLogPrefix2(this.getClass()));
    content.toIterable().forEach(System.out::println);
    System.out.println("-----------------------");
  }

  /**
   * test the chat client with a specific OllamaChatModel bean injected. <br>
   *
   */
  @Test
  public void TestSpecifyModel(@Autowired OllamaChatModel ollamaChatModel) {
    ChatClient chatClient = ChatClient.builder(ollamaChatModel)
     .build();
    Flux<String> content = chatClient.prompt()//
     .user("hello")//
     .stream()//
     .content();
    System.out.println(LogUtils.getLogPrefix2(this.getClass()));
    content.toIterable().forEach(System.out::println);
    System.out.println("-----------------------");
  }

  @Test
  public void TestPrompt() {
    ChatClient chatClient = chatclientBuilder//.defaultSystem(AiConstants.SYSTEM_PROMPT)
     .build();
    Flux<String> content = chatClient.prompt()//
     //.system(p -> p.param("name", "leon"))//
     .system(p -> p.text(AiConstants.SYSTEM_PROMPT).param("name", "leon"))//
     .user("what is my name?")//
     .stream()//
     .content();
    System.out.println(LogUtils.getLogPrefix2(this.getClass()));
    content.toIterable().forEach(System.out::println);
    System.out.println("-----------------------");
  }

  /**
   * test the chat client with a specific OllamaChatModel bean injected and with an advisor. <br>
   * need write below code in the application.properties to enable the advisor:
   * logging:
   * level:
   * org.springframework.ai.chat.client.advisor: DEBUG
   */
  @Test
  public void TestAdvisor() {
    ChatClient chatClient = chatclientBuilder.build();
    Flux<String> content = chatClient.prompt()//
     .advisors(new SimpleLoggerAdvisor())
     .user("hello")//
     .stream()//
     .content();
    System.out.println(LogUtils.getLogPrefix2(this.getClass()));
    content.toIterable().forEach(System.out::println);
    System.out.println("-----------------------");
  }

  /**
   * if the user input contains the word "hello", the SafeGuardAdvisor will block the request and return : <br>
   * "I'm unable to respond to that due to sensitive content. Could we rephrase or discuss something else?" <br>
   */
  @Test
  public void TestSafeGuardAdvisor() {
    ChatClient chatClient = chatclientBuilder.build();
    Flux<String> content = chatClient.prompt()//
     .advisors(new SafeGuardAdvisor(List.of("hello")))
     .user("hello, how are you")//
     .stream()//
     .content();
    System.out.println(LogUtils.getLogPrefix2(this.getClass()));
    content.toIterable().forEach(System.out::println);
    System.out.println("-----------------------");
  }

  @Test
  public void TestCustomAdvisor() {
    ChatClient chatClient = chatclientBuilder.defaultAdvisors(new SimpleLoggerAdvisor(), new ReReadAdvisor())
     .build();
    Flux<String> content = chatClient.prompt()//
     //.advisors(new ReReadAdvisor())
     .user("hello, how are you")//
     .stream()//
     .content();
    System.out.println(LogUtils.getLogPrefix2(this.getClass()));
    content.toIterable().forEach(System.out::println);
    System.out.println("-----------------------");
  }

  @Test
  public void TestChatMemory() {
    ChatMemory chatMemory = MessageWindowChatMemory.builder().build();
    String sessionId = "test-session-1";
    //
    ChatClient chatClient = chatclientBuilder.defaultAdvisors(new SimpleLoggerAdvisor())
     .build();

    // first message
    UserMessage userMsg1 = new UserMessage("My name is WaKa");
    chatMemory.add(sessionId, userMsg1);
    ChatResponse resp1 = chatClient.prompt(new Prompt(chatMemory.get(sessionId)))
     .call().chatResponse();
    chatMemory.add(sessionId, Objects.requireNonNull(resp1).getResult().getOutput());

    // second message
    UserMessage userMsg2 = new UserMessage("What is my name?");
    chatMemory.add(sessionId, userMsg2);
    ChatResponse resp2 = chatClient.prompt(new Prompt(chatMemory.get(sessionId)))
     .call().chatResponse();
    chatMemory.add(sessionId, Objects.requireNonNull(resp2).getResult().getOutput());
    //


    System.out.println(LogUtils.getLogPrefix2(this.getClass()));
    System.out.println("First response: " + resp2.getResult().getOutput().getText());
    System.out.println("-----------------------");
  }
}
