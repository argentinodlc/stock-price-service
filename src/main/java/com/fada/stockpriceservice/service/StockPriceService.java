package com.fada.stockpriceservice.service;

import com.fada.stockpriceservice.client.FinnhubWebSocketHandler;
import com.fada.stockpriceservice.event.StockPriceEvent;
import com.fada.stockpriceservice.model.StockPrice;
import com.fada.stockpriceservice.model.TradeData;
import com.fada.stockpriceservice.model.TradeMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class StockPriceService implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(StockPriceService.class);

    @Value("${finnhub.api-key}")
    private String apiKey;

    private final StandardWebSocketClient webSocketClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final FinnhubWebSocketHandler finnhubWebSocketHandler;
    private final ObjectMapper objectMapper;

    @Override
    public void run(@Value("${finnhub.api-key}") String... args) {
        String url = "wss://ws.finnhub.io?token=" + apiKey;
        LOG.info("Connecting to: {}", url);
        WebSocketConnectionManager connectionManager = new WebSocketConnectionManager(webSocketClient, finnhubWebSocketHandler, url);
        connectionManager.start();
    }

    @EventListener
    public void handleStockPriceEvent(StockPriceEvent event) {
        try {
            TradeMessage tradeMessage = objectMapper.readValue(event.getMessage(), TradeMessage.class);
            if (processTradeMessageType(tradeMessage)) return;
            tradeMessage.getData().forEach(data -> {
                treatStockPrice(data);
            });
        } catch (Exception e) {
            LOG.error("Error processing message", e);
        }
    }

    private boolean processTradeMessageType(TradeMessage tradeMessage) {
        switch (tradeMessage.getType()) {
            case PING:
                LOG.info("Received ping message");
                return true;
            case ERROR:
                LOG.error("Received error message: {}", tradeMessage.getMsg());
                return true;
            default:
                return false;
        }
    }

    private void treatStockPrice(TradeData data) {
        String symbol = data.getSymbol();
        Double price = data.getPrice();
        LocalDateTime time = LocalDateTime.ofEpochSecond(data.getTimestamp() / 1000, 0, ZoneOffset.UTC);
        createAndPublish(symbol, price, time);
    }

    private void createAndPublish(String symbol, Double price, LocalDateTime time) {
        StockPrice stockPrice = createStockPrice(symbol, price, time);
        publishToKafka(stockPrice);
    }

    private StockPrice createStockPrice(String symbol, Double price, LocalDateTime time) {
        return new StockPrice(symbol, BigDecimal.valueOf(price), time);
    }

    private void publishToKafka(StockPrice stockPrice) {
        LOG.info("Publishing to Kafka: {}", stockPrice);
        kafkaTemplate.send(kafkaTemplate.getDefaultTopic(), stockPrice.toString());
    }

}