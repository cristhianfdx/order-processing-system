package com.cristhianfdx.orderworker.service;

import com.cristhianfdx.orderworker.exceptions.GeneralException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FailedMessageServiceTest {

    @Mock
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOperations;

    @InjectMocks
    private FailedMessageService subject;

    private final String orderId = "order-123";
    private final String redisKey = "failed-order:" + orderId;
    private final String jsonBody = "{\"id\": \"order-123\"}";

    @Before
    public void setUp() {
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    public void shouldStoreFailedMessage() {
        when(valueOperations.set(eq(redisKey), eq("1|" + jsonBody))).thenReturn(Mono.just(true));

        StepVerifier.create(subject.storeFailedMessage(orderId, jsonBody, 1))
                .verifyComplete();
    }

    @Test
    public void testIncrementRetrySuccess() {
        when(valueOperations.get(redisKey)).thenReturn(Mono.just("2|" + jsonBody));
        when(valueOperations.set(eq(redisKey), eq("3|" + jsonBody))).thenReturn(Mono.just(true));

        StepVerifier.create(subject.incrementRetry(orderId))
                .expectNext(3)
                .verifyComplete();
    }

    @Test
    public void shouldIncrementRetryMalformedValue() {
        when(valueOperations.get(redisKey)).thenReturn(Mono.just("invalid-value"));

        StepVerifier.create(subject.incrementRetry(orderId))
                .expectErrorMatches(err -> err instanceof GeneralException &&
                        err.getMessage().contains("Malformed Redis value"))
                .verify();
    }

    @Test
    public void shouldGetRetryAndMessageSuccess() {
        when(valueOperations.get(redisKey)).thenReturn(Mono.just("5|" + jsonBody));

        StepVerifier.create(subject.getRetryAndMessage(orderId))
                .assertNext(tuple -> {
                    assertEquals(5, (int) tuple.getT1());
                    assertEquals(jsonBody, tuple.getT2());
                })
                .verifyComplete();
    }

    @Test
    public void shouldGetRetryAndMessageMalformedValue() {
        when(valueOperations.get(redisKey)).thenReturn(Mono.just("just-one-part"));

        StepVerifier.create(subject.getRetryAndMessage(orderId))
                .expectErrorMatches(err -> err instanceof GeneralException &&
                        err.getMessage().contains("Malformed Redis value"))
                .verify();
    }

    @Test
    public void shouldRemoveFailedMessage() {
        when(redisTemplate.delete(redisKey)).thenReturn(Mono.empty());

        StepVerifier.create(subject.removeFailedMessage(orderId))
                .verifyComplete();
    }
}