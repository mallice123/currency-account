package com.portfolio.recruitment.currencyaccount.api.controller;

import com.portfolio.recruitment.currencyaccount.api.dto.AccountCreationRequest;
import com.portfolio.recruitment.currencyaccount.api.dto.AccountDetailsResponse;
import com.portfolio.recruitment.currencyaccount.api.dto.AccountResponse;
import com.portfolio.recruitment.currencyaccount.business.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @RequestBody @Valid AccountCreationRequest accountCreationRequest) {
        AccountResponse response = accountService.createAccount(accountCreationRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDetailsResponse> getAccount(@PathVariable Long id) {
        AccountDetailsResponse response = accountService.getAccountById(id);
        return ResponseEntity.ok(response);
    }

}
