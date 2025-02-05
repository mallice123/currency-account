package com.portfolio.recruitment.currencyaccount.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public record ExchangeRate(
        String table,
        String currency,
        String code,
        List<Rate> rates
) {

    public record Rate(
            String no,
            String effectiveDate,
            @JsonProperty("bid") BigDecimal buyRate,
            @JsonProperty("ask") BigDecimal saleRate
    )
    {
    }
}