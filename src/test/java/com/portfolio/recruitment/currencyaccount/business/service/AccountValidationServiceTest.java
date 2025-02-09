package com.portfolio.recruitment.currencyaccount.business.service;


import com.portfolio.recruitment.currencyaccount.business.service.model.CurrencyNotSupported;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.portfolio.recruitment.currencyaccount.business.service.util.CurrencyConstants.CURRENCY_CODE_PLN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountValidationServiceTest {

    private static AccountValidationService accountValidationService;

    @BeforeAll
    static void setUp() {
        accountValidationService = new AccountValidationService();
    }

    @Test
    public void validateSufficientFunds_ShouldThrowException_WhenBalanceIsInsufficient() {
        BigDecimal balance = new BigDecimal("100.00");
        BigDecimal amount = new BigDecimal("1000.00");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, ()-> accountValidationService.validateSufficientFunds(balance, CURRENCY_CODE_PLN, amount));
        assertEquals("Insufficient initialValue in PLN.", exception.getMessage());
    }

    @Test
    public void validateCurrencyCode_ShouldThrowException_WhenCurrencyCodeIsInvalid() {
        CurrencyNotSupported exception = assertThrows(CurrencyNotSupported.class, ()-> accountValidationService.validateAvailableCurrency("KSH"));
        assertEquals("Provided currency is not supported: KSH", exception.getMessage());
    }


}