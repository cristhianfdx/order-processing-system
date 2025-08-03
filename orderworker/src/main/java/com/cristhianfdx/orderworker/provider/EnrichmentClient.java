package com.cristhianfdx.orderworker.provider;


import com.cristhianfdx.orderworker.config.ExternalAPIProperties;
import com.cristhianfdx.orderworker.config.RetryProperties;
import com.cristhianfdx.orderworker.dto.CustomerDTO;
import com.cristhianfdx.orderworker.dto.ProductDTO;
import com.cristhianfdx.orderworker.exceptions.CustomerNotFoundException;
import com.cristhianfdx.orderworker.exceptions.ExternalApiException;
import com.cristhianfdx.orderworker.exceptions.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnrichmentClient {

    private final WebClient webClient;
    private final RetryProperties retryProperties;
    private final ExternalAPIProperties externalAPIProperties;

    public Mono<CustomerDTO> getCustomerById(String customerId) {
        String url = externalAPIProperties.getCustomer() + "/" + customerId;

        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        response -> response.statusCode() == HttpStatus.NOT_FOUND
                                ? Mono.error(new CustomerNotFoundException(customerId))
                                : Mono.error(new ExternalApiException(
                                String.format("Error while retrieving customer %s. Status: %s", customerId, response.statusCode())
                        ))
                )
                .bodyToMono(CustomerDTO.class)
                .retryWhen(
                        Retry.backoff(
                                        retryProperties.getMaxAttempts(),
                                        Duration.ofMillis(retryProperties.getInitialIntervalMs())
                                )
                                .jitter(0.5)
                                .transientErrors(true)
                                .filter(throwable -> !(throwable instanceof CustomerNotFoundException))
                )
                .doOnSubscribe(s -> log.info("Fetching customer {}", customerId))
                .doOnError(e -> log.error("Error fetching customer {}: {}", customerId, e.getMessage(), e));
    }

    public Mono<ProductDTO> getProductById(String productId) {
        String url = externalAPIProperties.getProduct() + "/" + productId;

        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        response -> response.statusCode() == HttpStatus.NOT_FOUND
                                ? Mono.error(new ProductNotFoundException(productId))
                                : Mono.error(new ExternalApiException(
                                String.format("Error while retrieving product %s. Status: %s", productId, response.statusCode())
                        ))
                )
                .bodyToMono(ProductDTO.class)
                .retryWhen(
                        Retry.backoff(
                                        retryProperties.getMaxAttempts(),
                                        Duration.ofMillis(retryProperties.getInitialIntervalMs())
                                )
                                .jitter(0.5)
                                .transientErrors(true)
                                .filter(throwable -> !(throwable instanceof ProductNotFoundException))
                )
                .doOnSubscribe(s -> log.info("Fetching product {}", productId))
                .doOnError(e -> log.error("Error fetching product {}: {}", productId, e.getMessage(), e));
    }
}

