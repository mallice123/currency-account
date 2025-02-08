package com.portfolio.recruitment.currencyaccount.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AccountCreationRequest(
        @NotNull(message = "First name cannot be null.")
        @Size(min = 1, max = 20, message = "First name must be between 1 and 20 characters.")
        String firstName,

        @NotNull(message = "Last name cannot be null.")
        @Size(min = 1, max = 20, message = "Last name must be between 1 and 20 characters.")
        String lastName,

        @NotNull(message = "Currency code cannot be null.")
        String currencyCode,

        @NotNull(message = "Initial value cannot be null.")
        @DecimalMin(value = "0.00", message = "Initial value can't be a negative value.")
        BigDecimal initialBalance
)
{
}
