package com.fada.stockpriceservice.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class StockPriceEvent extends ApplicationEvent {
    private final String message;

    public StockPriceEvent(Object source, String message) {
        super(source);
        this.message = message;
    }

}