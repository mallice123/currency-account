package com.portfolio.recruitment.currencyaccount.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ExchangeRequest(

        @NotNull(message = "ID must not be null.")
        Long id,

        @NotNull(message = "Amount must not be null.")
        @Positive(message = "Amount must be greater than zero.")
        BigDecimal amount,

        @NotNull(message = "Initial currency must not be null.")
        String initialCurrency,

        @NotNull(message = "Target currency must not be null")
        String targetCurrency
)
{
}
