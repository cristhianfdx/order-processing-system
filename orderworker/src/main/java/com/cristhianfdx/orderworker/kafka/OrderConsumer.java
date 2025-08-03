package com.cristhianfdx.orderworker.kafka;

import com.cristhianfdx.orderworker.dto.OrderMessageDTO;
import com.cristhianfdx.orderworker.service.OrderProcessorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConsumer {
    private final ObjectMapper objectMapper;
    private final OrderProcessorService orderProcessorService;

    @KafkaListener(topics = "orders", groupId = "order-processor-group")
    public void consume(String message) {
        log.info("Kafka message received: {}", message);

        Mono.fromCallable(() -> objectMapper.readValue(message, OrderMessageDTO.class))
                .flatMap(orderProcessorService::processOrder)
                .subscribe(
                        unused -> log.info("Order processed successfully"),
                        error -> log.error("Failed to process Kafka message: {}", message, error)
                );
    }
}
