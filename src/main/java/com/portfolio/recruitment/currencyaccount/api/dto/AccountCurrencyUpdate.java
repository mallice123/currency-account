package com.portfolio.recruitment.currencyaccount.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Currency;

public record AccountCurrencyUpdate(

    @NotNull(message = "ID must not be null.")
    Long id,

    @NotNull(message = "Currency code cannot be null.")
    Currency currencyCode,

    @NotNull(message = "Value cannot be null.")
    @DecimalMin(value = "0.00", message = "Value cannot be negative.")
    BigDecimal value
)
{
}
