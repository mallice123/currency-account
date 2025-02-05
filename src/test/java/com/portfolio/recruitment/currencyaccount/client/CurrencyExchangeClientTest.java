package com.portfolio.recruitment.currencyaccount.client;

import com.github.benmanes.caffeine.cache.Cache;
import com.portfolio.recruitment.currencyaccount.api.dto.ExchangeRate;
import com.portfolio.recruitment.currencyaccount.business.service.model.ExchangeRateNotAvailableException;
import com.portfolio.recruitment.currencyaccount.connectors.exchangeClient.client.CurrencyExchangeClient;
import org.junit.jupiter.api.Tag;
import reactor.test.StepVerifier;
import com.portfolio.recruitment.currencyaccount.business.service.model.DataNotFoundException;
import com.portfolio.recruitment.currencyaccount.business.service.model.InvalidRequestException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Tag("unitTest")
class CurrencyExchangeClientTest {

    @Mock
    private WebClient.Builder webClientBuilder;
    @Mock
    private Cache<String, ExchangeRate> rateCache;
    @Mock
    private WebClient webClient;

    private CurrencyExchangeClient client;

    private final long MOCK_TIMEOUT_MILLIS = 500;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        CircuitBreaker circuitBreaker = CircuitBreaker.of("currencyExchangeClient",
                getCircuitBreakerConfig());

        String nbpApiBaseUrl = "http://test.api/";
        when(webClientBuilder.baseUrl(nbpApiBaseUrl)).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        ExchangeRate mockResponse = createExchangeRate(new BigDecimal("4.50"), new BigDecimal("4.00"), "USD");
        mockWebClient(mockResponse);

        client = new CurrencyExchangeClient("http://test.api/", webClientBuilder, rateCache, circuitBreaker,MOCK_TIMEOUT_MILLIS);
    }

    @Test
    void testGetSaleExchangeRateWithCachedRate() {
        // Given: Mock exchange rate present in cache
        ExchangeRate exchangeRate = createExchangeRate(new BigDecimal("4.50"), new BigDecimal("4.00"), "USD");
        when(rateCache.getIfPresent("USD")).thenReturn(exchangeRate);

        // When: Call the client to get the sale exchange rate
        BigDecimal result = client.getSaleExchangeRate("USD").block();

        // Then: Verify that the sale rate is correct
        assertEquals(new BigDecimal("4.00"), result);
    }

    @Test
    void testGetSaleExchangeRate_NotFound() {
        when(rateCache.getIfPresent(any())).thenReturn(null);
        when(webClient.get()).thenThrow(new DataNotFoundException("Data not found"));

        assertThrows(DataNotFoundException.class, () -> client.getSaleExchangeRate("USD").block());
    }

    @Test
    void testGetSaleExchangeRate_InvalidRequest() {
        // Given: Exchange rate is not present in cache and WebClient throws DataNotFoundException
        when(rateCache.getIfPresent(any())).thenReturn(null);
        when(webClient.get()).thenThrow(new InvalidRequestException("Invalid request"));

        // When & Then: Calling the client should throw DataNotFoundException
        assertThrows(InvalidRequestException.class, () -> client.getSaleExchangeRate("USD").block());
    }

    @Test
    void testGetSaleExchangeRate_NullCurrencyCode() {
        // Given: Null currency code passed to the method

        // When & Then: Calling the client should throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> client.getSaleExchangeRate(null).block());
    }

    @Test
    void testCircuitBreakerFallback() {
        // Given: Circuit breaker denies permission and fallback logic should be used
        when(rateCache.getIfPresent(any())).thenReturn(null);

        CircuitBreaker circuitBreakerMock = Mockito.mock(CircuitBreaker.class);
        when(circuitBreakerMock.tryAcquirePermission()).thenReturn(false);

        client = new CurrencyExchangeClient("http://test.api/", webClientBuilder, rateCache, circuitBreakerMock,MOCK_TIMEOUT_MILLIS);

        ExchangeRate dto = createExchangeRate(new BigDecimal("3.50"), new BigDecimal("4.50"), "USD");

        when(rateCache.getIfPresent("USD")).thenReturn(dto);
        // When: Call the client to get the sale exchange rate with fallback
        StepVerifier.create(client.getSaleExchangeRate("USD"))
                // Then: Verify that the fallback sale rate is returned correctly
                .expectNext(new BigDecimal("4.50"))
                .verifyComplete();
    }

    @Test
    void testFetchExchangeRate_Timeout() {
        // Given: Simulate a delay beyond the timeout duration
        when(rateCache.getIfPresent("USD")).thenReturn(null);

        WebClient.RequestHeadersUriSpec requestHeadersUriSpecMock = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.ResponseSpec responseSpecMock = Mockito.mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.uri(any(Function.class))).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.retrieve()).thenReturn(responseSpecMock);

        //When making a call with delay
        when(responseSpecMock.bodyToMono(ExchangeRate.class))
                .thenReturn(Mono.delay(Duration.ofMillis(MOCK_TIMEOUT_MILLIS + 1500))
                        .then(Mono.just(new ExchangeRate(null, null, "USD", null))));

        when(rateCache.getIfPresent("USD")).thenReturn(null);
        // When & Then: Verify that the timeout exception is triggered
        StepVerifier.create(client.getSaleExchangeRate("USD"))
                .expectErrorMatches(throwable ->
                        throwable instanceof ExchangeRateNotAvailableException &&
                                throwable.getMessage().contains("Timeout while fetching exchange rate"))
                .verify();
    }

    private CircuitBreakerConfig getCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(10)
                .build();
    }

    private void mockWebClient(ExchangeRate mockResponse) {

        WebClient.RequestHeadersUriSpec requestHeadersUriSpecMock = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.ResponseSpec responseSpecMock = Mockito.mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpecMock);

        when(requestHeadersUriSpecMock.uri(any(Function.class))).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.retrieve()).thenReturn(responseSpecMock);

        when(responseSpecMock.bodyToMono(ExchangeRate.class)).thenReturn(Mono.just(mockResponse));
    }

    private ExchangeRate createExchangeRate(BigDecimal saleRate, BigDecimal purchaseRate, String currencyCode) {
        ExchangeRate.Rate mockRate = new ExchangeRate.Rate(
                null,
                null,
                purchaseRate,
                saleRate
        );
        return new ExchangeRate(
                null,
                null,
                currencyCode,
                java.util.List.of(mockRate)
        );
    }

}