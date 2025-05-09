package com.example.task;

import com.example.task.model.KrakenOrderBookSnapshot;
import com.example.task.model.KrakenOrderBookUpdate;
import com.example.task.model.KrakenPriceLevel;
import com.example.task.model.KrakenTicker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class KrakenWebSocketListener implements WebSocket.Listener {
  private static final String SUBSCRIPTION_MESSAGE = "{\"event\":\"subscribe\",\"pair\":[\"BTC/USD\",\"ETH/USD\"],\"subscription\":{\"name\":\"book\"}}";
  private static final String TICKER_SUBSCRIPTION = "{\"event\":\"subscribe\",\"pair\":[\"BTC/USD\",\"ETH/USD\"],\"subscription\":{\"name\":\"ticker\"}}";
  private static final String TICKER = "ticker";
  private static final String ORDER_BOOK = "book-10";

  private final OrderBookHolder orderBookHolder;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void onOpen(WebSocket webSocket) {
    webSocket.sendText(SUBSCRIPTION_MESSAGE, true);
    webSocket.sendText(TICKER_SUBSCRIPTION, true);
    WebSocket.Listener.super.onOpen(webSocket);
  }

  @Override
  public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
    handleMessage(data.toString());
    return WebSocket.Listener.super.onText(webSocket, data, last);
  }

  @Override
  public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
    return WebSocket.Listener.super.onBinary(webSocket, data, last);
  }

  @Override
  public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
    webSocket.sendPong(message);
    return WebSocket.Listener.super.onPing(webSocket, message);
  }

  @Override
  public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
    log.warn("WebSocket closed: {}", reason);
    return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
  }

  @Override
  public void onError(WebSocket webSocket, Throwable error) {
    log.error("WebSocket error: {}", error.getMessage());
    WebSocket.Listener.super.onError(webSocket, error);
  }

  private void handleMessage(String message) {
    try {
      JsonNode rootNode = objectMapper.readTree(message);
      JsonNode dataNode = rootNode.get(1);
      JsonNode channelName = rootNode.get(2);
      if (null != channelName && ORDER_BOOK.equals(channelName.asText())) {
        if (dataNode.has("as") && dataNode.has("bs")) {
          KrakenOrderBookSnapshot snapshot = objectMapper.treeToValue(dataNode, KrakenOrderBookSnapshot.class);
          snapshot.setPair(rootNode.get(3).asText());
          handleSnapshot(snapshot);
        } else if (dataNode.has("a") || dataNode.has("b")) {
          KrakenOrderBookUpdate update = objectMapper.treeToValue(dataNode, KrakenOrderBookUpdate.class);
          update.setPair(rootNode.get(3).asText());
          handleUpdate(update);
        }
      } else if (null != channelName && TICKER.equals(channelName.asText())) {
        KrakenTicker ticker = objectMapper.treeToValue(dataNode, KrakenTicker.class);
        ticker.setPair(rootNode.get(3).asText());
        handleTicker(ticker);
      }
    } catch (Exception e) {
      log.error("Error handling message: {}", message, e);
    }
  }

  private void handleSnapshot(KrakenOrderBookSnapshot snapshot) {
    OrderBook orderBook = orderBookHolder.get(snapshot.getPair());
    updateOrderBook(snapshot.getSnapshotAsks(), orderBook::updateAsk);
    updateOrderBook(snapshot.getSnapshotBids(), orderBook::updateBid);
    orderBookHolder.printOrderBook();
  }

  private void handleUpdate(KrakenOrderBookUpdate update) {
    OrderBook orderBook = orderBookHolder.get(update.getPair());
    updateOrderBook(update.getAsks(), orderBook::updateAsk);
    updateOrderBook(update.getBids(), orderBook::updateBid);
    orderBookHolder.printOrderBook();
  }

  private void handleTicker(KrakenTicker ticker) {
    OrderBook orderBook = orderBookHolder.get(ticker.getPair());
    orderBook.setBestAsk(buildPriceLevelTicker(ticker.getBestAsk()));
    orderBook.setBestBid(buildPriceLevelTicker(ticker.getBestBid()));
    orderBookHolder.printOrderBook();
  }

  private void updateOrderBook(List<List<String>> priceLevels, Consumer<KrakenPriceLevel> updateMethod) {
    if (priceLevels != null) {
      priceLevels.forEach(price -> {
        KrakenPriceLevel priceLevel = buildPriceLevelOrderBook(price);
        updateMethod.accept(priceLevel);
      });
    }
  }

  private KrakenPriceLevel buildPriceLevelOrderBook(List<String> priceLevel) {
    return KrakenPriceLevel.builder()
        .price(new BigDecimal(priceLevel.get(0)))
        .size(new BigDecimal(priceLevel.get(1)))
        .timestamp(priceLevel.get(2))
        .build();
  }

  private KrakenPriceLevel buildPriceLevelTicker(List<String> priceLevel) {
    return KrakenPriceLevel.builder()
        .price(new BigDecimal(priceLevel.get(0)))
        .size(new BigDecimal(priceLevel.get(2)))
        .build();
  }
}
