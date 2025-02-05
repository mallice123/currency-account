package com.portfolio.recruitment.currencyaccount.business.service.conversion.strategies;

import com.portfolio.recruitment.currencyaccount.connectors.exchangeClient.client.CurrencyExchangeClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public final class ForeignCurrencyToPLNStrategy implements CurrencyConversionStrategy{

    @Override
    public boolean supports(String fromCurrency, String toCurrency) {
        return toCurrency.equals("PLN");
    }

    @Override
    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency, CurrencyExchangeClient exchangeClient) {
        BigDecimal buyRate = exchangeClient.getSaleExchangeRate(fromCurrency).block();
        return amount.multiply(buyRate).setScale(2, RoundingMode.HALF_UP);
    }
}