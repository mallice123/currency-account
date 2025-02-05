package com.portfolio.recruitment.currencyaccount.business.service.conversion.strategies;

import com.portfolio.recruitment.currencyaccount.connectors.exchangeClient.client.CurrencyExchangeClient;

import java.math.BigDecimal;

public interface CurrencyConversionStrategy {
    boolean supports(String fromCurrency, String toCurrency);
    BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency, CurrencyExchangeClient exchangeClient);
}
