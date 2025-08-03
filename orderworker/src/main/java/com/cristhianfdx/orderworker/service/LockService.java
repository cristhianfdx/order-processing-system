package com.cristhianfdx.orderworker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class LockService {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public Mono<Boolean> tryLock(String key, Duration ttl) {
        return redisTemplate.opsForValue()
                .setIfAbsent(key, "locked", ttl);
    }

    public Mono<Void> releaseLock(String key) {
        return redisTemplate.delete(key).then();
    }
}
