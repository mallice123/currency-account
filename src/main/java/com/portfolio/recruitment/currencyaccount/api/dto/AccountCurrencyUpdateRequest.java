package com.portfolio.recruitment.currencyaccount.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountCurrencyUpdateRequest(

    @NotNull(message = "ID must not be null.")
    Long id,

    @NotNull(message = "Currency code cannot be null.")
    String currencyCode,

    @NotNull(message = "Value cannot be null.")
    @DecimalMin(value = "0.00", message = "Value cannot be negative.")
    BigDecimal initialValue
)
{
}
