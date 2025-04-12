package com.rdtech.chat_server.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class MessageService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final Duration TTL = Duration.ofHours(1);

    public MessageService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void storeMessage(String toPubKey, String message) {
        String key = "queue:" + toPubKey;

        log.info("Chave Redis: {}", key);
        log.info("Mensagem: {}", message);

        redisTemplate.opsForList().rightPush(key, message);
        redisTemplate.expire(key, TTL);
    }

    public List<String> retrieveMessages(String toPubKey) {
        String key = "queue:" + toPubKey;
        List<String> messages = redisTemplate.opsForList().range(key, 0, -1);
        redisTemplate.delete(key);
        return messages != null ? messages : Collections.emptyList();
    }
}
