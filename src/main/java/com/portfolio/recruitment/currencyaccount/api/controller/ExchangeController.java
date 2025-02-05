package com.portfolio.recruitment.currencyaccount.api.controller;

import com.portfolio.recruitment.currencyaccount.api.dto.ConversionResponse;
import com.portfolio.recruitment.currencyaccount.api.dto.ExchangeRequest;
import com.portfolio.recruitment.currencyaccount.business.service.CurrencyConversionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exchange")
public class ExchangeController {

    private final CurrencyConversionService currencyConversionService;

    public ExchangeController(CurrencyConversionService currencyConversionService) {
        this.currencyConversionService = currencyConversionService;
    }

    @PostMapping
    public ResponseEntity<ConversionResponse> convertCurrency(@RequestBody @Valid ExchangeRequest exchangeRequest
    ) {
        ConversionResponse response = currencyConversionService.exchangeCurrency(exchangeRequest);
        return ResponseEntity.ok(response);
    }

}
