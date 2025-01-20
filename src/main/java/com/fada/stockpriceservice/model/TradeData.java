package com.fada.stockpriceservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TradeData {
    @JsonProperty("s")
    private String symbol;
    @JsonProperty("p")
    private Double price;
    @JsonProperty("t")
    private Long timestamp;
    @JsonProperty("v")
    private Double volume;

    @Override
    public String toString() {
        return "TradeData{" +
                "symbol='" + symbol + '\'' +
                ", price=" + price +
                ", timestamp=" + timestamp +
                ", volume=" + volume +
                '}';
    }
}