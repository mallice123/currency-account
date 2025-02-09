package com.portfolio.recruitment.currencyaccount.business.service;

import com.portfolio.recruitment.currencyaccount.api.dto.AccountCurrencyUpdateRequest;
import com.portfolio.recruitment.currencyaccount.business.service.model.Account;
import com.portfolio.recruitment.currencyaccount.business.service.model.CurrencyNotSupported;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.portfolio.recruitment.currencyaccount.business.service.util.CurrencyConstants.AVAILABLE_CURRENCY;

@Service
public class AccountValidationService {

    protected void validateSufficientFunds(BigDecimal balance, String currency, BigDecimal amountToDeduct) {
        if (balance.compareTo(amountToDeduct) < 0) {
            throw new IllegalArgumentException("Insufficient initialValue in " + currency + ".");
        }
    }

    protected void validateAvailableCurrency(String currencyCode) {
        if (!AVAILABLE_CURRENCY.contains(currencyCode)) {
            throw new CurrencyNotSupported("Provided currency is not supported: " + currencyCode);
        }
    }

    protected void validateAvailableCurrency(String initialCurrency, String targetCurrency) {
        if (!AVAILABLE_CURRENCY.contains(initialCurrency)) {
            throw new CurrencyNotSupported("Provided currency is not supported: " + initialCurrency);
        }
        if (!AVAILABLE_CURRENCY.contains(targetCurrency)) {
            throw new CurrencyNotSupported("Provided currency is not supported: " + targetCurrency);
        }
    }
    protected boolean validateCurrencyExists(AccountCurrencyUpdateRequest accountCurrencyUpdateRequest, Account account) {

         return (account.balances().stream().anyMatch(
                 balance -> balance.currencyCode().equals(accountCurrencyUpdateRequest.currencyCode())));
    }

}