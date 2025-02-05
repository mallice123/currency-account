package com.portfolio.recruitment.currencyaccount.api.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.portfolio.recruitment.currencyaccount.api.serialization.CurrencyDeserializer;
import com.portfolio.recruitment.currencyaccount.api.serialization.CurrencySerializer;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Currency;

public record ExchangeRequest(

        @NotNull(message = "ID must not be null.")
        Long id,

        @NotNull(message = "Amount must not be null.")
        @Positive(message = "Amount must be greater than zero.")
        BigDecimal amount,

        @NotNull(message = "Initial currency must not be null.")
        @JsonDeserialize(using = CurrencyDeserializer.class)
        @JsonSerialize(using = CurrencySerializer.class)
        Currency initialCurrency,

        @NotNull(message = "Target currency must not be null")
        @JsonDeserialize(using = CurrencyDeserializer.class)
        @JsonSerialize(using = CurrencySerializer.class)
        Currency targetCurrency
)
{
}
