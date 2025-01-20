package com.fada.stockpriceservice.client;

import com.fada.stockpriceservice.event.StockPriceEvent;
import com.fada.stockpriceservice.model.Ticker;
import com.fada.stockpriceservice.repository.TickerRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;

@Component
@AllArgsConstructor
public class FinnhubWebSocketHandler extends TextWebSocketHandler {

    private static final Logger LOG = LoggerFactory.getLogger(FinnhubWebSocketHandler.class);

    private ApplicationEventPublisher eventPublisher;
    private TickerRepository tickerRepository;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        LOG.info("Connected to WebSocket server");
        List<Ticker> tickers = tickerRepository.findAll();
        if (tickers.isEmpty()) {
            LOG.warn("No tickers found");
            return;
        }
        tickers.stream()
                .map(ticker -> String.format("{\"type\":\"subscribe\",\"symbol\":\"%s\"}", ticker.getSymbol()))
                .forEach(payload -> {
                    LOG.info("Sending {}", payload);
                    try {
                        session.sendMessage(new TextMessage(payload));
                    } catch (Exception e) {
                        LOG.error("Error sending message", e);
                    }
                });
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        LOG.info("Received message: {}", message.getPayload());
        eventPublisher.publishEvent(new StockPriceEvent(this, message.getPayload()));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        LOG.error("Transport error", exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        LOG.info("Connection closed [{}]", status.getReason());
    }
}