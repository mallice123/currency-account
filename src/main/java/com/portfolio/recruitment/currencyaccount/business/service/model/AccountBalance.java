package com.portfolio.recruitment.currencyaccount.business.service.model;

import java.math.BigDecimal;
import java.util.Currency;

public record AccountBalance(
        String currencyCode,
        BigDecimal value
)
{
}
