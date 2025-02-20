package com.portfolio.recruitment.currencyaccount.business.service.util;

import java.util.Set;

public class CurrencyConstants {
    public static final String CURRENCY_CODE_PLN = "PLN";
    public static final String CURRENCY_CODE_USD = "USD";
    public static final String CURRENCY_CODE_EUR = "EUR";
    public static final String CURRENCY_CODE_GBP = "GBP";

    public static final Set<String> AVAILABLE_CURRENCY = Set.of(CURRENCY_CODE_PLN, CURRENCY_CODE_USD, CURRENCY_CODE_EUR, CURRENCY_CODE_GBP);
}
