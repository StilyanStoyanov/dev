package com.example.task;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OrderBookHolder {
  private final Map<String, OrderBook> orderBookMap = new ConcurrentHashMap<>();

  public OrderBook get(String productId) {
    return orderBookMap.computeIfAbsent(productId, OrderBook::new);
  }

}
