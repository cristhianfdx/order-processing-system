package com.cristhianfdx.orderworker.service;

import com.cristhianfdx.orderworker.dto.CustomerStatusEnum;
import com.cristhianfdx.orderworker.dto.OrderMessageDTO;
import com.cristhianfdx.orderworker.dto.ProductDTO;
import com.cristhianfdx.orderworker.exceptions.CustomerNotFoundException;
import com.cristhianfdx.orderworker.exceptions.InactiveCustomerException;
import com.cristhianfdx.orderworker.exceptions.OrderAlreadyExists;
import com.cristhianfdx.orderworker.exceptions.ProductNotFoundException;
import com.cristhianfdx.orderworker.model.Order;
import com.cristhianfdx.orderworker.model.Product;
import com.cristhianfdx.orderworker.provider.EnrichmentClient;
import com.cristhianfdx.orderworker.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderProcessorService {

    private static final int MAX_RETRIES = 3;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(1);

    private final EnrichmentClient enrichmentClient;
    private final OrderRepository orderRepository;
    private final LockService lockService;
    private final FailedMessageService failedMessageService;
    private final ObjectMapper objectMapper;

    public Mono<Void> processOrder(OrderMessageDTO orderMessage) {
        String lockKey = "lock:" + orderMessage.getOrderId();

        return lockService.tryLock(lockKey, LOCK_DURATION)
                .flatMap(acquired -> {
                    if (!acquired) {
                        log.warn("Lock not acquired for order {}", orderMessage.getOrderId());
                        return Mono.empty();
                    }

                    return orderRepository.findById(orderMessage.getOrderId())
                            .flatMap(existing -> {
                                log.info("Order {} already exists. Skipping processing.", orderMessage.getOrderId());
                                return Mono.<Void>error(new OrderAlreadyExists(orderMessage.getOrderId()));
                            })
                            .switchIfEmpty(
                                    enrichOrder(orderMessage)
                                            .flatMap(orderRepository::save)
                                            .doOnSuccess(o -> log.info("Order {} saved successfully", o.getOrderId()))
                                            .then()
                            )
                            .onErrorResume(e -> handleProcessingError(orderMessage, e))
                            .doFinally(signal -> lockService.releaseLock(lockKey).subscribe());
                });
    }

    private Mono<Order> enrichOrder(OrderMessageDTO message) {
        return enrichmentClient.getCustomerById(message.getCustomerId())
                .flatMap(customer -> {
                    if (customer.getStatus() != CustomerStatusEnum.ACTIVE) {
                        return Mono.error(new InactiveCustomerException(customer.getId()));
                    }
                    return Mono.just(customer);
                })
                .flatMap(customer -> {
                    List<String> productIds = message.getProducts();
                    return Flux.fromIterable(productIds)
                            .flatMap(enrichmentClient::getProductById)
                            .map(this::buildProduct)
                            .collectList()
                            .map(products -> buildOrder(message, products));
                });
    }

    private Product buildProduct(ProductDTO dto) {
        return Product.builder()
                .productId(dto.getId())
                .name(dto.getName())
                .price(dto.getPrice())
                .build();
    }

    private Order buildOrder(OrderMessageDTO message, List<Product> products) {
        return Order.builder()
                .orderId(message.getOrderId())
                .customerId(message.getCustomerId())
                .products(products)
                .build();
    }

    private Mono<Void> handleProcessingError(OrderMessageDTO message, Throwable e) {
        log.error("Failed to process order {}: {}", message.getOrderId(), e.getMessage(), e);

        if (shouldNotRetry(e)) {
            return failedMessageService.storeFailedMessage(
                    message.getOrderId(),
                    e.getMessage(),
                    0
            );
        }

        return failedMessageService.incrementRetry(message.getOrderId())
                .defaultIfEmpty(1)
                .flatMap(retryCount -> {
                    if (retryCount >= MAX_RETRIES) {
                        log.error("Max retries reached for order {}. Skipping.", message.getOrderId());
                        return Mono.empty();
                    }

                    return failedMessageService.storeFailedMessage(
                            message.getOrderId(),
                            e.getMessage(),
                            retryCount
                    );
                })
                .onErrorResume(redisEx -> {
                    log.error("Redis failure while handling order {}: {}", message.getOrderId(), redisEx.getMessage(), redisEx);
                    return Mono.empty();
                });


    }

    private static boolean shouldNotRetry(Throwable e) {
        return e instanceof CustomerNotFoundException
                || e instanceof ProductNotFoundException
                || e instanceof InactiveCustomerException
                || e instanceof OrderAlreadyExists;
    }


}



