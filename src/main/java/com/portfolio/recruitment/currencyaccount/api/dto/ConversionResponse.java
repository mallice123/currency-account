package com.portfolio.recruitment.currencyaccount.api.dto;

import java.math.BigDecimal;
import java.util.Currency;

public record ConversionResponse(
        OriginalCurrency originalCurrency,
        ConvertedCurrency convertedCurrency
) {

     public record OriginalCurrency(
            BigDecimal originalCurrencyBalance,
            Currency originalCurrencyCode
    )
    {
    }

    public record ConvertedCurrency(
            BigDecimal convertedCurrencyBalance,
            Currency convertedCurrencyCode
    )
    {
    }
}