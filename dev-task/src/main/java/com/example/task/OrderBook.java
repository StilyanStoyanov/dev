package com.example.task;

import com.example.task.model.KrakenPriceLevel;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import ch.qos.logback.core.util.StringUtil;

@Getter
@Setter
public class OrderBook {
  private final String productId;
  private final TreeMap<BigDecimal, BigDecimal> bids = new TreeMap<>(Comparator.reverseOrder());
  private final TreeMap<BigDecimal, BigDecimal> asks = new TreeMap<>(Comparator.reverseOrder());
  private KrakenPriceLevel bestBid;
  private KrakenPriceLevel bestAsk;
  private String timestamp;

  public OrderBook(String productId) {
    this.productId = productId;
  }

  public void updateBid(KrakenPriceLevel priceLevel) {
    updateOrderBook(bids, priceLevel);
  }

  public void updateAsk(KrakenPriceLevel priceLevel) {
    updateOrderBook(asks, priceLevel);
  }

  private void updateOrderBook(Map<BigDecimal, BigDecimal> orderBook, KrakenPriceLevel priceLevel) {
    if (priceLevel.getSize().compareTo(BigDecimal.ZERO) == 0) {
      orderBook.remove(priceLevel.getPrice());
    } else {
      orderBook.put(priceLevel.getPrice(), priceLevel.getSize());
    }
    this.timestamp = priceLevel.getTimestamp();
  }
}
