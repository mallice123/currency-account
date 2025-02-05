package com.portfolio.recruitment.currencyaccount.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.portfolio.recruitment.currencyaccount.api.dto.ExchangeRate;
import com.portfolio.recruitment.currencyaccount.app.config.CacheConfig;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class CacheConfigTest {

    @Test
    void testRateCacheConfiguration() {
        CacheConfig cacheConfig = new CacheConfig();
        Cache<String, ExchangeRate> rateCache = cacheConfig.rateCache();

        assertThat(rateCache).isNotNull();
        assertThat(rateCache.policy().eviction().get().getMaximum()).isEqualTo(100L);
        assertThat(rateCache.policy().expireAfterWrite().get().getExpiresAfter()).isEqualTo(Duration.ofMinutes(10));
    }
}