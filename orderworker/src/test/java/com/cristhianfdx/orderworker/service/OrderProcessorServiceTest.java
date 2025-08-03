package com.cristhianfdx.orderworker.service;

import com.cristhianfdx.orderworker.dto.CustomerDTO;
import com.cristhianfdx.orderworker.dto.CustomerStatusEnum;
import com.cristhianfdx.orderworker.dto.OrderMessageDTO;
import com.cristhianfdx.orderworker.dto.ProductDTO;
import com.cristhianfdx.orderworker.exceptions.InactiveCustomerException;
import com.cristhianfdx.orderworker.exceptions.OrderAlreadyExists;
import com.cristhianfdx.orderworker.model.Order;
import com.cristhianfdx.orderworker.model.Product;
import com.cristhianfdx.orderworker.provider.EnrichmentClient;
import com.cristhianfdx.orderworker.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OrderProcessorServiceTest {

    @Mock
    private EnrichmentClient enrichmentClient;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private LockService lockService;

    @Mock
    private FailedMessageService failedMessageService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OrderProcessorService subject;

    private final String ORDER_ID = "order123";
    private final String CUSTOMER_ID = "customer123";


    @Test
    public void shouldSkipProcessingWhenLockNotAcquired() {
        OrderMessageDTO message = OrderMessageDTO.builder()
                .orderId(ORDER_ID)
                .customerId(CUSTOMER_ID)
                .build();

        when(lockService.tryLock(anyString(), any())).thenReturn(Mono.just(false));

        StepVerifier.create(subject.processOrder(message))
                .verifyComplete();

        verify(lockService).tryLock(eq("lock:" + ORDER_ID), any());
        verifyNoInteractions(orderRepository, enrichmentClient);
    }

    @Test
    public void shouldProcessAndSaveOrderSuccessfully() {
        CustomerDTO customer = CustomerDTO.builder()
                .id(CUSTOMER_ID)
                .status(CustomerStatusEnum.ACTIVE)
                .build();

        ProductDTO product1 = ProductDTO.builder()
                .id("product-1")
                .name("Product 1")
                .price(10)
                .build();

        ProductDTO product2 = ProductDTO.builder()
                .id("product-2")
                .name("Product 2")
                .price(20)
                .build();

        Order expectedOrder = Order.builder()
                .orderId(ORDER_ID)
                .customerId(CUSTOMER_ID)
                .products(List.of(
                        Product.builder().productId("product-1").name("Product 1").price(10).build(),
                        Product.builder().productId("product-2").name("Product 2").price(20).build()
                ))
                .build();

        OrderMessageDTO orderMessageDTO = OrderMessageDTO.builder()
                .orderId(ORDER_ID)
                .customerId(CUSTOMER_ID)
                .products(List.of("product-1", "product-2"))
                .build();

        when(lockService.tryLock("lock:" + ORDER_ID, Duration.ofMinutes(1))).thenReturn(Mono.just(true));
        when(orderRepository.findById(ORDER_ID)).thenReturn(Mono.empty());
        when(enrichmentClient.getCustomerById(CUSTOMER_ID)).thenReturn(Mono.just(customer));
        when(enrichmentClient.getProductById("product-1")).thenReturn(Mono.just(product1));
        when(enrichmentClient.getProductById("product-2")).thenReturn(Mono.just(product2));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(expectedOrder));
        when(lockService.releaseLock("lock:" + ORDER_ID)).thenReturn(Mono.empty());

        StepVerifier.create(subject.processOrder(orderMessageDTO))
                .verifyComplete();

        verify(orderRepository).save(any(Order.class));
    }
}