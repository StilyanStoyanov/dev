package com.example.task;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CountDownLatch;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
@RequiredArgsConstructor
public class WebSocketClient {
  private static final String WEBSOCKET_URL = "wss://ws.kraken.com";
  private final CountDownLatch latch = new CountDownLatch(1);
  private WebSocket webSocket;

  @PostConstruct
  public void start() {
    HttpClient client = HttpClient.newHttpClient();
    webSocket = client.newWebSocketBuilder()
        .buildAsync(URI.create(WEBSOCKET_URL), new KrakenWebSocketListener())
        .join();

    System.out.println("WebSocket connection started");

    try {
      latch.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      System.out.println("WebSocket listener interrupted");
    }
  }

  @PreDestroy
  public void stop() {
    System.out.println("Stopping WebSocket connection...");
    if (webSocket != null) {
      webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Bye")
          .thenRun(() -> {
            System.out.println("WebSocket closed");
            latch.countDown();
          });
    } else {
      latch.countDown();
    }
  }
}
