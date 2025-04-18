package com.rdtech.chat_server.ws;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionManager {
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void addSession(String pubKey, WebSocketSession session) {
        sessions.put(pubKey, session);
    }

    public void removeSession(WebSocketSession session) {
        sessions.values().remove(session);
    }

    public WebSocketSession getSession(String pubKey) {
        return sessions.get(pubKey);
    }

    public Map<String, WebSocketSession> getAllSessions() {
        return new ConcurrentHashMap<>(sessions);
    }
}
