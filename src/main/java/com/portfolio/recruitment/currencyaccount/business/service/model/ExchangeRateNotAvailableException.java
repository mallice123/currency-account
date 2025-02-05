package com.portfolio.recruitment.currencyaccount.business.service.model;

public class ExchangeRateNotAvailableException extends RuntimeException {

    public ExchangeRateNotAvailableException(String message) {
        super(message);
    }

    public ExchangeRateNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

}
