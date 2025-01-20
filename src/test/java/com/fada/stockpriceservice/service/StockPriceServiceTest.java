package com.fada.stockpriceservice.service;

import com.fada.stockpriceservice.event.StockPriceEvent;
import com.fada.stockpriceservice.model.MessageType;
import com.fada.stockpriceservice.model.TradeData;
import com.fada.stockpriceservice.model.TradeMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StockPriceServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private StockPriceService stockPriceService;

    @Captor
    private ArgumentCaptor<String> kafkaMessageCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleStockPriceEvent_shouldProcessTradeMessage() throws Exception {
        String message = "{\"type\":\"trade\",\"data\":[{\"s\":\"AAPL\",\"p\":150.0,\"t\":1633024800000}]}";
        StockPriceEvent event = new StockPriceEvent(this, message);
        TradeMessage tradeMessage = new TradeMessage();
        tradeMessage.setType(MessageType.TRADE);
        TradeData tradeData = new TradeData();
        tradeData.setSymbol("AAPL");
        tradeData.setPrice(150.0);
        tradeData.setTimestamp(1633024800000L);
        tradeMessage.setData(Collections.singletonList(tradeData));

        when(objectMapper.readValue(message, TradeMessage.class)).thenReturn(tradeMessage);
        when(kafkaTemplate.getDefaultTopic()).thenReturn("default-topic");

        stockPriceService.handleStockPriceEvent(event);

        verify(kafkaTemplate, times(1)).send(eq("default-topic"), kafkaMessageCaptor.capture());
        String kafkaMessage = kafkaMessageCaptor.getValue();
        assertTrue(kafkaMessage.contains("AAPL"));
        assertTrue(kafkaMessage.contains("150.0"));
    }

    @Test
    void handleStockPriceEvent_shouldHandlePingMessage() throws Exception {
        String message = "{\"type\":\"ping\"}";
        StockPriceEvent event = new StockPriceEvent(this, message);
        TradeMessage tradeMessage = new TradeMessage();
        tradeMessage.setType(MessageType.PING);

        when(objectMapper.readValue(message, TradeMessage.class)).thenReturn(tradeMessage);

        stockPriceService.handleStockPriceEvent(event);

        verify(kafkaTemplate, never()).send(anyString(), anyString());
    }

    @Test
    void handleStockPriceEvent_shouldHandleErrorMessage() throws Exception {
        String message = "{\"type\":\"error\",\"msg\":\"Invalid API key\"}";
        StockPriceEvent event = new StockPriceEvent(this, message);
        TradeMessage tradeMessage = new TradeMessage();
        tradeMessage.setType(MessageType.ERROR);
        tradeMessage.setMsg("Invalid API key");

        when(objectMapper.readValue(message, TradeMessage.class)).thenReturn(tradeMessage);

        stockPriceService.handleStockPriceEvent(event);

        verify(kafkaTemplate, never()).send(anyString(), anyString());
    }

    @Test
    void createAndPublish_shouldPublishStockPriceToKafka() throws Exception {
        String symbol = "AAPL";
        Double price = 150.0;
        LocalDateTime time = LocalDateTime.ofEpochSecond(1633024800, 0, ZoneOffset.UTC);

        Method createAndPublishMethod = StockPriceService.class.getDeclaredMethod("createAndPublish", String.class, Double.class, LocalDateTime.class);
        createAndPublishMethod.setAccessible(true);

        when(kafkaTemplate.getDefaultTopic()).thenReturn("default-topic");

        createAndPublishMethod.invoke(stockPriceService, symbol, price, time);

        verify(kafkaTemplate, times(1)).send(eq("default-topic"), kafkaMessageCaptor.capture());
        String kafkaMessage = kafkaMessageCaptor.getValue();
        assertTrue(kafkaMessage.contains("AAPL"));
        assertTrue(kafkaMessage.contains("150.0"));
    }
}