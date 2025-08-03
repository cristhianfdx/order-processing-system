package com.cristhianfdx.orderworker.service;

import com.cristhianfdx.orderworker.exceptions.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Service
@RequiredArgsConstructor
public class FailedMessageService {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private static final String FAILED_MESSAGE_PREFIX = "failed-order:";
    private static final String DELIMITER = "\\|";
    private static final String DELIMITER_RAW = "|";

    public Mono<Void> storeFailedMessage(String orderId, String json, int retryCount) {
        String key = buildKey(orderId);
        String value = retryCount + DELIMITER_RAW + json;
        return redisTemplate.opsForValue().set(key, value).then();
    }

    public Mono<Integer> incrementRetry(String orderId) {
        String key = buildKey(orderId);
        return redisTemplate.opsForValue().get(key)
                .flatMap(value -> {
                    String[] parts = value.split(DELIMITER, 2);
                    if (parts.length < 2) {
                        return Mono.error(new GeneralException("Malformed Redis value for key: " + key));
                    }
                    int newRetry = Integer.parseInt(parts[0]) + 1;
                    String json = parts[1];
                    String newValue = newRetry + DELIMITER_RAW + json;
                    return redisTemplate.opsForValue().set(key, newValue)
                            .thenReturn(newRetry);
                });
    }

    public Mono<Tuple2<Integer, String>> getRetryAndMessage(String orderId) {
        String key = buildKey(orderId);
        return redisTemplate.opsForValue().get(key)
                .flatMap(value -> {
                    String[] parts = value.split(DELIMITER, 2);
                    if (parts.length < 2) {
                        return Mono.error(new GeneralException("Malformed Redis value for key: " + key));
                    }
                    return Mono.just(Tuples.of(Integer.parseInt(parts[0]), parts[1]));
                });
    }

    public Mono<Void> removeFailedMessage(String orderId) {
        return redisTemplate.delete(buildKey(orderId)).then();
    }

    private String buildKey(String orderId) {
        return FAILED_MESSAGE_PREFIX + orderId;
    }
}
