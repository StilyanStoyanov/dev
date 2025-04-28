package com.example.task.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KrakenOrderBookSnapshot {
  private String pair;
  @JsonProperty("as")
  private List<List<String>> snapshotAsks;
  @JsonProperty("bs")
  private List<List<String>> snapshotBids;
}
