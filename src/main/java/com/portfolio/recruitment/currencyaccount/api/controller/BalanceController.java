package com.portfolio.recruitment.currencyaccount.api.controller;

import com.portfolio.recruitment.currencyaccount.api.dto.AccountCurrencyUpdate;
import com.portfolio.recruitment.currencyaccount.business.service.AccountBalanceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account/currencies")
public class BalanceController {

    private final AccountBalanceService accountBalanceService;

    public BalanceController(AccountBalanceService accountBalanceService) {
        this.accountBalanceService = accountBalanceService;
    }

    @PatchMapping()
    public ResponseEntity<Void> addNewBalanceType(
            @RequestBody @Valid AccountCurrencyUpdate accountCurrencyUpdate) {
        accountBalanceService.addNewBalanceType(accountCurrencyUpdate);
        return ResponseEntity.noContent().build();
    }

}
