package com.portfolio.recruitment.currencyaccount.business.service;

import com.portfolio.recruitment.currencyaccount.business.service.model.CurrencyNotSupported;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Currency;

import static com.portfolio.recruitment.currencyaccount.business.service.util.CurrencyConstants.AVAILABLE_CURRENCY;

@Service
public class AccountValidationService {

    protected void validateSufficientFunds(BigDecimal balance, String currency, BigDecimal amountToDeduct) {
        if (balance.compareTo(amountToDeduct) < 0) {
            throw new IllegalArgumentException("Insufficient value in " + currency + ".");
        }
    }

    protected void validateAvailableCurrency(String currencyCode) {
        if (!AVAILABLE_CURRENCY.contains(currencyCode)) {
            throw new CurrencyNotSupported("Provided currency is not supported: " + currencyCode);
        }
    }

    protected void validateAvailableCurrency(Currency initialCurrency, Currency targetCurrency) {
        if (!AVAILABLE_CURRENCY.contains(initialCurrency.getCurrencyCode())) {
            throw new CurrencyNotSupported("Provided currency is not supported: " + initialCurrency.getCurrencyCode());
        }
        if (!AVAILABLE_CURRENCY.contains(targetCurrency.getCurrencyCode())) {
            throw new CurrencyNotSupported("Provided currency is not supported: " + targetCurrency.getCurrencyCode());
        }
    }

}