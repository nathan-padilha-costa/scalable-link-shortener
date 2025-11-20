package com.linkshortener.demo.service;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;

    private static final int MAX_REQUESTS_PER_MINUTE =10;

    public boolean allowRequest (String ipAdress) {
        String key = "rate_limit:" + ipAdress;

        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count ==1) {
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }

        return count != null && count <= MAX_REQUESTS_PER_MINUTE;
    }
    
}
