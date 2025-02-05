package com.portfolio.recruitment.currencyaccount.business.service.conversion;

import com.portfolio.recruitment.currencyaccount.business.service.conversion.strategies.CurrencyConversionStrategy;
import com.portfolio.recruitment.currencyaccount.connectors.exchangeClient.client.CurrencyExchangeClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CurrencyConversionManager {

    private final List<CurrencyConversionStrategy> strategies;

    public CurrencyConversionManager(List<CurrencyConversionStrategy> strategies) {
        this.strategies = strategies;
    }

    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency, CurrencyExchangeClient exchangeClient) {
        CurrencyConversionStrategy strategy = strategies.stream()
                .filter(s -> s.supports(fromCurrency, toCurrency))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException(
                        "No strategy available for " + fromCurrency + " to " + toCurrency
                ));

        return strategy.convert(amount, fromCurrency, toCurrency, exchangeClient);
    }
}