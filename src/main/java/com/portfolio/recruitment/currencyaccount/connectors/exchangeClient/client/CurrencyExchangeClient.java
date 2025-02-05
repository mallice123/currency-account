package com.portfolio.recruitment.currencyaccount.connectors.exchangeClient.client;

import com.github.benmanes.caffeine.cache.Cache;
import com.portfolio.recruitment.currencyaccount.api.dto.ExchangeRate;
import com.portfolio.recruitment.currencyaccount.business.service.model.DataNotFoundException;
import com.portfolio.recruitment.currencyaccount.business.service.model.ExchangeRateNotAvailableException;
import com.portfolio.recruitment.currencyaccount.business.service.model.InvalidRequestException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;

import com.portfolio.recruitment.currencyaccount.app.config.ExchangeRateEndpoints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class CurrencyExchangeClient {

    private final WebClient webClient;
    private final Cache<String, ExchangeRate> rateCache;
    private final CircuitBreaker circuitBreaker;
    private final Duration requestTimeout;

    public CurrencyExchangeClient(@Value("${nbp.api.base.url}") String nbpApiBaseUrl,
                                  WebClient.Builder webClientBuilder,
                                  Cache<String, ExchangeRate> rateCache,
                                  CircuitBreaker circuitBreaker,
                                  @Value("${exchange.client.timeout}") long timeoutMillis) {
        this.requestTimeout = Duration.ofMillis(timeoutMillis);
        this.webClient = webClientBuilder.baseUrl(nbpApiBaseUrl).build();
        this.rateCache = rateCache;
        this.circuitBreaker = circuitBreaker;
    }

    private Mono<ExchangeRate> fetchExchangeRate(String currencyCode) {
        log.info("Fetching exchange rate from cache for currency: {}", currencyCode);
        return Mono.justOrEmpty(rateCache.getIfPresent(currencyCode))
                .switchIfEmpty(
                        webClient.get()
                                .uri(uriBuilder ->
                                        uriBuilder
                                                .path(ExchangeRateEndpoints.EXCHANGERATES_RATES_C)
                                                .queryParam("format", "json")
                                                .build(currencyCode)
                                )
                                .retrieve()
                                .bodyToMono(ExchangeRate.class)
                                .timeout(requestTimeout)
                                .doOnNext(response -> {
                                    log.info("Successfully fetched exchange rate from API: {}", response);
                                    rateCache.put(currencyCode, response);
                                })
                                .onErrorMap(throwable -> {
                                    if (throwable instanceof TimeoutException) {
                                        log.warn("Timeout occurred while fetching exchange rate for currency: {}", currencyCode);
                                        return new ExchangeRateNotAvailableException("Timeout while fetching exchange rate.", throwable);
                                    }
                                    return handleApiError(throwable);
                                })
                                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                                        .filter(throwable -> throwable instanceof TimeoutException))
                                .doFinally(signalType -> log.info("API call completed for currency: {} with signal: {}", currencyCode, signalType))
                )
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorResume(throwable -> {
                    if (throwable instanceof ExchangeRateNotAvailableException &&
                            throwable.getMessage().contains("Timeout while fetching exchange rate")) {
                        return Mono.error(throwable);
                    }
                    return Mono.justOrEmpty(rateCache.getIfPresent(currencyCode))
                            .switchIfEmpty(Mono.error(
                                    new ExchangeRateNotAvailableException("Exchange rate not available in a fallback response.")));
                });
    }

    private Mono<ExchangeRate> getExchangeRate(String currencyCode) {
        validateCurrencyCode(currencyCode);
        return fetchExchangeRate(currencyCode).filter(
                CurrencyExchangeClient::isExchangeRateValid).switchIfEmpty(
                        Mono.error(new ExchangeRateNotAvailableException("Invalid exchange data for currency: " + currencyCode)));
    }

    public Mono<BigDecimal> getSaleExchangeRate(String currencyCode) {
        return getExchangeRate(currencyCode)
                .map(exchangeRate -> exchangeRate.rates().getFirst().buyRate());
    }

    public Mono<BigDecimal> getPurchaseExchangeRate(String currencyCode) {
        return getExchangeRate(currencyCode)
                .map(exchangeRate -> exchangeRate.rates().getFirst().saleRate());
    }

    private static Boolean isExchangeRateValid(ExchangeRate exchangeRate) {
        return Optional.ofNullable(exchangeRate)
                .map(dto -> dto.rates() != null && !dto.rates().isEmpty())
                .orElse(false);
    }

    private void validateCurrencyCode(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new IllegalArgumentException("Currency code must not be null or blank.");
        }
    }

    private Throwable handleApiError(Throwable throwable) {
        if (throwable instanceof WebClientResponseException ex) {
            int statusCode = ex.getStatusCode().value();
            String errorMessage = Optional.of(ex.getResponseBodyAsString()).orElse("No error body provided by API.");

            return switch (statusCode) {
                case 404 -> new DataNotFoundException("Exchange rate data not found. Response: " + errorMessage);
                case 400 -> {
                    // Differentiates between generic 400 and "limit exceeded" case
                    if (errorMessage.contains("Przekroczony limit")) {
                        yield new InvalidRequestException("Invalid request: Exceeded data limit. Response: " + errorMessage);
                    }
                    yield new InvalidRequestException("Invalid request: Bad parameters provided. Response: " + errorMessage);
                }
                default -> new ExchangeRateNotAvailableException(
                        "Unexpected error during API call. Status Code: " + statusCode + ", Response: " + errorMessage, ex);
            };
        }

        return new ExchangeRateNotAvailableException("An unexpected error occurred while fetching exchange rate.", throwable);
    }
}
