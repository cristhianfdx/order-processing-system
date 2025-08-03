package com.cristhianfdx.orderworker.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LockServiceTest {

    @Mock
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOperations;

    @InjectMocks
    private LockService subject;

    private static final String KEY = "lock:order:123";
    private static final Duration TTL = Duration.ofSeconds(10);


    @Before
    public void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    public void shouldTryLockSuccess() {
        when(valueOperations.setIfAbsent(KEY, "locked", TTL)).thenReturn(Mono.just(true));
        StepVerifier.create(subject.tryLock(KEY, TTL))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    public void shouldTryLockFail() {
        when(valueOperations.setIfAbsent(KEY, "locked", TTL)).thenReturn(Mono.just(false));

        StepVerifier.create(subject.tryLock(KEY, TTL))
                .expectNext(false)
                .verifyComplete();
    }
}