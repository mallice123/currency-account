package com.portfolio.recruitment.currencyaccount.business.service.conversion.strategies;

import com.portfolio.recruitment.currencyaccount.connectors.exchangeClient.client.CurrencyExchangeClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public final class PLNToForeignCurrencyStrategy implements CurrencyConversionStrategy {

    @Override
    public boolean supports(String fromCurrency, String toCurrency) {
        return fromCurrency.equals("PLN");
    }

    @Override
    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency, CurrencyExchangeClient exchangeClient) {
        BigDecimal saleRate = exchangeClient.getPurchaseExchangeRate(toCurrency).block();
        return amount.divide(saleRate, 2, RoundingMode.HALF_UP);
    }

}