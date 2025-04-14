package com.rdtech.chat_server.consumer;

import com.rdtech.chat_server.ws.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

    private final SessionManager sessionManager;

    @KafkaListener(topics = "chat-mensagens", groupId = "chat-server")
    public void consume(String message) {
        log.info("Kafka -> WS: {}", message);
        log.info("Mensagem consumida com sucesso!");
    }
}
