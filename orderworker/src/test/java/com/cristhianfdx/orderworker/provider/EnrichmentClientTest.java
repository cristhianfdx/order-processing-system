package com.cristhianfdx.orderworker.provider;

import com.cristhianfdx.orderworker.config.ExternalAPIProperties;
import com.cristhianfdx.orderworker.config.RetryProperties;
import com.cristhianfdx.orderworker.dto.CustomerDTO;
import com.cristhianfdx.orderworker.dto.ProductDTO;
import com.cristhianfdx.orderworker.exceptions.CustomerNotFoundException;
import com.cristhianfdx.orderworker.exceptions.ProductNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EnrichmentClientTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private RetryProperties retryProperties;

    @Mock
    private ExternalAPIProperties externalAPIProperties;

    @InjectMocks
    private EnrichmentClient subject;


    @Before
    public void setUp() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        when(retryProperties.getMaxAttempts()).thenReturn(3);
        when(retryProperties.getInitialIntervalMs()).thenReturn(100L);
        when(externalAPIProperties.getCustomer()).thenReturn("http://localhost:3000/api/customers");
        when(externalAPIProperties.getProduct()).thenReturn("http://localhost:3000/api/products");

        subject = new EnrichmentClient(webClient, retryProperties, externalAPIProperties);
    }

    @Test
    public void shouldGetCustomerByIdSuccess() {
        String customerId = "123";
        CustomerDTO expectedCustomer = CustomerDTO.builder()
                .id(customerId)
                .name("John Doe")
                .email("example@mail.com")
                .build();

        when(responseSpec.onStatus(Mockito.any(), Mockito.any()))
                .thenReturn(responseSpec);
        when(responseSpec.bodyToMono(CustomerDTO.class))
                .thenReturn(Mono.just(expectedCustomer));

        Mono<CustomerDTO> result = subject.getCustomerById(customerId);

        StepVerifier.create(result)
                .expectNext(expectedCustomer)
                .verifyComplete();
    }

    @Test
    public void shouldReturnExceptionIfGetCustomerNotFound() {
        String customerId = "404";

        when(responseSpec.onStatus(Mockito.any(), Mockito.any()))
                .thenReturn(responseSpec);
        when(responseSpec.bodyToMono(CustomerDTO.class))
                .thenReturn(Mono.error(new CustomerNotFoundException(customerId)));

        Mono<CustomerDTO> result = subject.getCustomerById(customerId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof CustomerNotFoundException &&
                        throwable.getMessage().contains("Customer not found with ID: 404"))
                .verify();
    }

    @Test
    public void shouldGetProductByIdSuccess() {
        String productId = "product-123";
        ProductDTO expectedProduct = ProductDTO.builder()
                .id(productId)
                .name("Laptop")
                .description("ASUS 16 inches")
                .price(10)
                .build();

        when(responseSpec.onStatus(Mockito.any(), Mockito.any()))
                .thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ProductDTO.class))
                .thenReturn(Mono.just(expectedProduct));

        Mono<ProductDTO> result = subject.getProductById(productId);

        StepVerifier.create(result)
                .expectNext(expectedProduct)
                .verifyComplete();
    }

    @Test
    public void shouldReturnExceptionIfGetProductNotFound() {
        String productId = "404";

        when(responseSpec.onStatus(Mockito.any(), Mockito.any()))
                .thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ProductDTO.class))
                .thenReturn(Mono.error(new ProductNotFoundException(productId)));

        Mono<ProductDTO> result = subject.getProductById(productId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ProductNotFoundException &&
                        throwable.getMessage().contains("Product not found with ID: 404"))
                .verify();
    }


}

