package com.rdtech.chat_server.scheduler;

import com.rdtech.chat_server.ws.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketPingScheduler {

    private final SessionManager sessionManager;

    @Scheduled(fixedRate = 10000)
    public void sendPings() {
        for (Map.Entry<String, WebSocketSession> entry : sessionManager.getAllSessions().entrySet()) {
            String pubKey = entry.getKey();
            WebSocketSession session = entry.getValue();

            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage("{\"type\":\"ping\"}"));
                    log.debug("Ping enviado para {}", pubKey);
                } catch (IOException e) {
                    log.error("Falha ao enviar ping para {}: {}", pubKey, e.getMessage());
                    sessionManager.removeSession(session);
                }
            } else {
                log.warn("Sessão não está mais aberta para {}", pubKey);
                sessionManager.removeSession(session);
            }
        }
    }
}
