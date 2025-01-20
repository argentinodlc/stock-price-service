package com.fada.stockpriceservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum MessageType {
    @JsonProperty("trade")
    TRADE,
    @JsonProperty("ping")
    PING,
    @JsonProperty("error")
    ERROR
}
