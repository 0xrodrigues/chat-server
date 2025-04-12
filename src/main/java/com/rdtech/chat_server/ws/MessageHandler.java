package com.rdtech.chat_server.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rdtech.chat_server.publisher.KafkaPublisher;
import com.rdtech.chat_server.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Component
@Slf4j
public class MessageHandler implements WebSocketHandler {

    private final KafkaPublisher kafkaPublisher;
    private final SessionManager sessionManager;
    private final MessageService messageService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessionManager.addSession(session);
        log.info("Cliente conectado: {}", session.getId());

        String pubKey = extractPubKeyFromSession(session);
        List<String> pending = messageService.retrieveMessages(pubKey);

        for (String msg : pending) {
            try {
                session.sendMessage(new TextMessage(msg));
                log.info("Entregando mensagem pendente para {}: {}", pubKey, msg);
            } catch (Exception e) {
                log.error("Erro ao entregar mensagem pendente: {}", e.getMessage());
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionManager.removeSession(session);
        log.info("Cliente desconectado: {}", session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        if (message instanceof TextMessage text) {
            String json = text.getPayload();
            log.info("JSON recebido: {}", json);

            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(json);
                String to = node.get("to").asText();

                messageService.storeMessage(to, json);
                log.info("Armazenando mensagem para: {}", to);

                kafkaPublisher.send("chat-mensagens", json);

            } catch (Exception e) {
                log.info("❌ Erro ao processar mensagem: {}", e.getMessage());
            }
        }
    }

    private String extractPubKeyFromSession(WebSocketSession session) {
        String query = Objects.requireNonNull(session.getUri()).getQuery();
        if (query != null && query.startsWith("pub=")) {
            return query.substring(4);
        } else {
            throw new IllegalStateException("Pubkey não encontrada na URL de conexão");
        }
    }

    @Override public void handleTransportError(WebSocketSession session, Throwable ex) {}
    @Override public boolean supportsPartialMessages() { return false; }
}
