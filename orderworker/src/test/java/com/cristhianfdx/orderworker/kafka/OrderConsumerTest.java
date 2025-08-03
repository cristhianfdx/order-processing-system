package com.cristhianfdx.orderworker.kafka;

import com.cristhianfdx.orderworker.dto.OrderMessageDTO;
import com.cristhianfdx.orderworker.service.OrderProcessorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OrderConsumerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private OrderProcessorService orderProcessorService;

    @InjectMocks
    private OrderConsumer orderConsumer;

    @Test
    public void shouldProcessValidMessageSuccessfully() throws Exception {
        String kafkaMessage = "{\"orderId\":\"order123\",\"customerId\":\"customerXYZ\",\"products\":[\"prod1\",\"prod2\"]}";
        OrderMessageDTO orderMessageDTO = OrderMessageDTO.builder()
                .orderId("order-123")
                .customerId("customer-abc")
                .products(List.of("product-1", "product-2"))
                .build();

        when(objectMapper.readValue(kafkaMessage, OrderMessageDTO.class))
                .thenReturn(orderMessageDTO);
        when(orderProcessorService.processOrder(any(OrderMessageDTO.class)))
                .thenReturn(Mono.empty());


        orderConsumer.consume(kafkaMessage);

        verify(objectMapper, times(1)).readValue(kafkaMessage, OrderMessageDTO.class);
        verify(orderProcessorService, times(1)).processOrder(orderMessageDTO);
    }

    @Test
    public void shouldLogErrorWhenJsonIsInvalid() throws Exception {
        String invalidMessage = "invalid-json";

        doAnswer(invocation -> {
            throw new IOException("Invalid JSON");
        }).when(objectMapper).readValue(anyString(), eq(OrderMessageDTO.class));

        orderConsumer.consume(invalidMessage);

        Thread.sleep(100);

        verify(objectMapper).readValue(invalidMessage, OrderMessageDTO.class);
        verifyNoInteractions(orderProcessorService);
    }
}