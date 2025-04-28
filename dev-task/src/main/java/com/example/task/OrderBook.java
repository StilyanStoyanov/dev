package com.example.task;

import com.example.task.model.KrakenPriceLevel;

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
  private String timestamp = "";

  public OrderBook(String productId) {
    this.productId = productId;
  }

  public void updateBid(KrakenPriceLevel priceLevel) {
    updateOrderBook(bids, priceLevel);
  }

  public void updateAsk(KrakenPriceLevel priceLevel) {
    updateOrderBook(asks, priceLevel);
  }

  public void print() {
    System.out.println("<----------------->");
    System.out.println("asks:");
    printPriceLevels(asks);
    printBestBidAsk();
    System.out.println("bids:");
    printPriceLevels(bids);
    printDateTime();
    System.out.println(productId);
    System.out.println(">-----------------<");
  }

  private void printBestBidAsk() {
    System.out.println("best bid (order book - level 2): [" + (bids.isEmpty() ? "N/A" : String.format("%.2f, %s", bids.firstEntry().getKey(), bids.firstEntry().getValue())) + "]");
    System.out.println("best ask (order book - level 2): [" + (asks.isEmpty() ? "N/A" : String.format("%.2f, %s", asks.lastEntry().getKey(), asks.lastEntry().getValue())) + "]");
    System.out.println("best bid (ticker - level 1): [" + (bestBid == null ? "N/A" : String.format("%.2f, %s", bestBid.getPrice(), bestBid.getSize() + "]")));
    System.out.println("best ask (ticker - level 1): [" + (bestAsk == null ? "N/A" : String.format("%.2f, %s", bestAsk.getPrice(), bestAsk.getSize() + "]")));
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

  private void updateOrderBook(Map<BigDecimal, BigDecimal> orderBook, KrakenPriceLevel priceLevel) {
    if (priceLevel.getSize().compareTo(BigDecimal.ZERO) == 0) {
      orderBook.remove(priceLevel.getPrice());
    } else {
      orderBook.put(priceLevel.getPrice(), priceLevel.getSize());
    }
    this.timestamp = priceLevel.getTimestamp();
  }

  private void printDateTime() {
    if (StringUtil.isNullOrEmpty(timestamp)) {
      return;
    }
    BigDecimal seconds = new BigDecimal(timestamp);
    Instant instant = Instant.ofEpochSecond(seconds.longValue(), seconds.remainder(BigDecimal.ONE).movePointRight(9).longValue());
    System.out.println(DateTimeFormatter.ISO_DATE_TIME.format(instant.atOffset(ZoneOffset.UTC)));
  }
}
