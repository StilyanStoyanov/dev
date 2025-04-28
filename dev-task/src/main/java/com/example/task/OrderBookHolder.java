package com.example.task;

import ch.qos.logback.core.util.StringUtil;
import com.example.task.model.KrakenPriceLevel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OrderBookHolder {
  private final Map<String, OrderBook> orderBookMap = new ConcurrentHashMap<>();

  public OrderBook get(String productId) {
    return orderBookMap.computeIfAbsent(productId, OrderBook::new);
  }

  public void printOrderBook() {
    orderBookMap.forEach(this::printOrderBook);
  }

  private void printOrderBook(String productId, OrderBook orderBook) {
    System.out.println("<----------------->");
    System.out.println("asks:");
    printPriceLevels(orderBook.getAsks());
    printBestBidAsk(orderBook);
    System.out.println("bids:");
    printPriceLevels(orderBook.getBids());
    printDateTime(orderBook.getTimestamp());
    System.out.println(productId);
    System.out.println(">-----------------<");
  }

  private void printBestBidAsk(OrderBook orderBook) {
    System.out.println("best bid (order book - level 2): [" + (orderBook.getBids().isEmpty() ? "N/A" : String.format("%.2f, %s", orderBook.getBids().firstEntry().getKey(), orderBook.getBids().firstEntry().getValue())) + "]");
    System.out.println("best ask (order book - level 2): [" + (orderBook.getAsks().isEmpty() ? "N/A" : String.format("%.2f, %s", orderBook.getAsks().lastEntry().getKey(), orderBook.getAsks().lastEntry().getValue())) + "]");
    System.out.println("best bid (ticker - level 1): [" + (orderBook.getBestBid() == null ? "N/A" : String.format("%.2f, %s", orderBook.getBestBid().getPrice(), orderBook.getBestBid().getSize() + "]")));
    System.out.println("best ask (ticker - level 1): [" + (orderBook.getBestAsk() == null ? "N/A" : String.format("%.2f, %s", orderBook.getBestAsk().getPrice(), orderBook.getBestAsk().getSize() + "]")));
  }

  private void printPriceLevels(TreeMap<BigDecimal, BigDecimal> priceLevels) {
    StringBuilder formattedAsks = new StringBuilder();
    formattedAsks.append("[");
    priceLevels.forEach((price, size) -> formattedAsks.append(String.format("[%.2f, %s],\n", price, size)));
    if (formattedAsks.length() > 1) {
      formattedAsks.setLength(formattedAsks.length() - 2); // Remove last ",\n"
    }
    formattedAsks.append("]");
    System.out.println(formattedAsks);
  }

  private void printDateTime(String timestamp) {
    if (StringUtil.isNullOrEmpty(timestamp)) {
      return;
    }
    BigDecimal seconds = new BigDecimal(timestamp);
    Instant instant = Instant.ofEpochSecond(seconds.longValue(), seconds.remainder(BigDecimal.ONE).movePointRight(9).longValue());
    System.out.println(DateTimeFormatter.ISO_DATE_TIME.format(instant.atOffset(ZoneOffset.UTC)));
  }
}
