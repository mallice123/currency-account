package com.portfolio.recruitment.currencyaccount.config;

import com.portfolio.recruitment.currencyaccount.app.config.CurrencyCircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyCircuitBreakerConfigTest {

    @Test
    void testCircuitBreakerConfiguration() {
        // Given: Create an instance of CurrencyCircuitBreakerConfig
        CurrencyCircuitBreakerConfig config = new CurrencyCircuitBreakerConfig();
        // When: Retrieve the CircuitBreaker and its configuration
        CircuitBreaker circuitBreaker = config.circuitBreaker();
        CircuitBreakerConfig breakerConfig = circuitBreaker.getCircuitBreakerConfig();

        // Then: Assert that the CircuitBreaker configuration matches the expected values
        assertThat(breakerConfig.getFailureRateThreshold()).isEqualTo(50.0f);
        assertThat(breakerConfig.getSlidingWindowSize()).isEqualTo(10);
        assertThat(breakerConfig.getMinimumNumberOfCalls()).isEqualTo(5);

        // Then: Verify the wait duration in the open state is as expected
        long waitDurationMillis = breakerConfig.getWaitIntervalFunctionInOpenState().apply(1);
        assertThat(waitDurationMillis).isEqualTo(1000L);
    }
}