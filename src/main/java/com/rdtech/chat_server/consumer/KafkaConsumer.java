package com.rdtech.chat_server.consumer;

import com.rdtech.chat_server.ws.SessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private final SessionManager sessionManager;

    @KafkaListener(topics = "chat-mensagens", groupId = "chat-server")
    public void consume(String message) {
        System.out.println("📥 Kafka -> WS: " + message);

        for (WebSocketSession session : sessionManager.getAllSessions()) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (Exception e) {
                System.out.println("⚠️ Erro ao enviar mensagem para " + session.getId());
            }
        }
    }
}
