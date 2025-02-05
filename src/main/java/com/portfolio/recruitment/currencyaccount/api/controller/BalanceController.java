package com.portfolio.recruitment.currencyaccount.api.controller;

import com.portfolio.recruitment.currencyaccount.api.dto.AccountCurrencyUpdate;
import com.portfolio.recruitment.currencyaccount.api.dto.AccountDetailsResponse;
import com.portfolio.recruitment.currencyaccount.business.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/balance")
public class BalanceController {

    private final AccountService accountService;

    public BalanceController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PatchMapping()
    public ResponseEntity<AccountDetailsResponse> addNewBalanceType(
            @RequestBody AccountCurrencyUpdate accountCurrencyUpdate) {
        AccountDetailsResponse updatedAccount = accountService.addNewBalanceType(accountCurrencyUpdate);
        return ResponseEntity.ok(updatedAccount);
    }

}
