package com.fada.stockpriceservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TradeMessage {
    private MessageType type;
    private List<TradeData> data;
    private String msg;

    @Override
    public String toString() {
        return "TradeMessage{" +
                "type=" + type +
                ", data=" + data +
                '}';
    }

}