package com.linkshortener.demo.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ShortCodeGenerator {
    
    private final StringRedisTemplate redisTemplate;
    private final Base62Encoder base62Encoder;

    private static final String COUNTER_KEY = "global_link_id";

    private static final long INITIAL_OFFSET = 100000L;

    public String generate(){

        Long uniqueId = redisTemplate.opsForValue().increment(COUNTER_KEY);
        
        if (uniqueId == null) {
            throw new RuntimeException("Redis connection failed durin ID generation");
        }

        long ajustedId = uniqueId + INITIAL_OFFSET;
        return base62Encoder.encode(ajustedId);
    }
}
