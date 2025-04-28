package com.example.task.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class KrakenPriceLevel {
  private BigDecimal price;
  private BigDecimal size;
  private String timestamp;
}
