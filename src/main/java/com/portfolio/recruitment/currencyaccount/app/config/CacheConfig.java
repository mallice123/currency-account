package com.portfolio.recruitment.currencyaccount.app.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.portfolio.recruitment.currencyaccount.api.dto.ExchangeRate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CacheConfig {
        @Bean
        public Cache<String, ExchangeRate> rateCache() {
            return Caffeine.newBuilder()
                    .maximumSize(100)
                    .expireAfterWrite(Duration.ofMinutes(10))
                    .build();
        }

}
