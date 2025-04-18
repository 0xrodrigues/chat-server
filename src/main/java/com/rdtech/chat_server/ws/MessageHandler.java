package com.rdtech.chat_server.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rdtech.chat_server.publisher.KafkaPublisher;
import com.rdtech.chat_server.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Component
@Slf4j
public class MessageHandler implements WebSocketHandler {

    private final SessionManager sessionManager;
    private final MessageService messageService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        String pubKey = null;

        if (query != null) {
            for (String param : query.split("&")) {
                if (param.startsWith("pub=")) {
                    pubKey = param.substring("pub=".length());
                    break;
                }
            }
        }

        if (pubKey != null && !pubKey.isEmpty()) {
            sessionManager.addSession(pubKey, session);
            log.info("Cliente conectado: {} (pubKey: {})", session.getId(), pubKey);

            List<String> pendingMessages = messageService.retrieveMessages(pubKey);
            log.info("Encontradas {} mensagens pendentes para {}", pendingMessages.size(), pubKey);

            for (String msg : pendingMessages) {
                try {
                    session.sendMessage(new TextMessage(msg));
                    log.info("Enviando pendente: {}", msg);
                } catch (Exception e) {
                    log.error("Erro ao enviar mensagem pendente: {}", e.getMessage());
                }
            }

        } else {
            log.warn("Conexão sem pubKey. Encerrando sessão...");
            try {
                session.close();
            } catch (Exception e) {
                log.error("Erro ao fechar sessão inválida", e);
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
            String messageAsString = text.getPayload();
            log.info("JSON recebido: {}", messageAsString);

            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(messageAsString);
                String to = json.get("to").asText();

                WebSocketSession targetSession = sessionManager.getSession(to);

                if (targetSession != null && targetSession.isOpen()) {
                    targetSession.sendMessage(new TextMessage(messageAsString));
                    log.info("🔄 Mensagem entregue diretamente para: {}", to);
                } else {
                    messageService.storeMessage(to, messageAsString);
                    log.info("📦 Destinatário offline, mensagem salva no Redis para: {}", to);
                }

            } catch (Exception e) {
                log.error("Erro ao processar mensagem: {}", e.getMessage(), e);
            }
        } else {
            log.warn("Mensagem WebSocket ignorada: tipo não suportado ({})", message.getClass().getSimpleName());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable ex) {
        log.error("Erro de transporte WebSocket: {}", ex.getMessage());
        sessionManager.removeSession(session);
        try {
            session.close();
        } catch (IOException e) {
            log.error("Erro ao fechar sessão após falha de transporte: {}", e.getMessage());
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
