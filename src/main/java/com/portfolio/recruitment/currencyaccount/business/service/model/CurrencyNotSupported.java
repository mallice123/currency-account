package com.portfolio.recruitment.currencyaccount.business.service.model;

public class CurrencyNotSupported extends RuntimeException {

    public CurrencyNotSupported(String message) {
        super(message);
    }
}
