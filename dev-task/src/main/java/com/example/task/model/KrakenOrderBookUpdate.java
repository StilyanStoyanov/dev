package com.example.task.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KrakenOrderBookUpdate {
  private String pair;
  @JsonProperty("b")
  private List<List<String>> asks;
  @JsonProperty("a")
  private List<List<String>> bids;
}