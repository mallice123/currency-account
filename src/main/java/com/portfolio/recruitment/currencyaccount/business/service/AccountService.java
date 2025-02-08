package com.portfolio.recruitment.currencyaccount.business.service;

import com.portfolio.recruitment.currencyaccount.business.service.model.AccountBalance;
import com.portfolio.recruitment.currencyaccount.api.dto.AccountCreationRequest;
import com.portfolio.recruitment.currencyaccount.api.dto.AccountDetailsResponse;
import com.portfolio.recruitment.currencyaccount.api.dto.AccountResponse;
import com.portfolio.recruitment.currencyaccount.business.service.mapper.AccountMapper;
import com.portfolio.recruitment.currencyaccount.business.service.model.AccountNotFound;
import com.portfolio.recruitment.currencyaccount.connectors.db.entity.AccountEntity;
import com.portfolio.recruitment.currencyaccount.business.service.model.Account;
import com.portfolio.recruitment.currencyaccount.connectors.db.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final AccountValidationService accountValidationService;

    public AccountService(AccountRepository accountRepository,
                          AccountMapper accountMapper, AccountValidationService accountValidationService) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
        this.accountValidationService = accountValidationService;
    }

    public AccountResponse createAccount(AccountCreationRequest accountCreationRequest) {
        accountValidationService.validateAvailableCurrency(accountCreationRequest.currencyCode());

        Account account = new Account(
                null,
                accountCreationRequest.firstName(),
                accountCreationRequest.lastName(),
                List.of(new AccountBalance(accountCreationRequest.currencyCode(), accountCreationRequest.initialBalance()))
        );
        AccountEntity savedAccount = accountRepository.save(accountMapper.toAccountEntity(account));
        return new AccountResponse(savedAccount.getId());
    }

    public AccountDetailsResponse getAccountById(Long accountId) {
        Account account =  accountRepository.findById(accountId)
                .map(accountMapper::toAccount)
                .orElseThrow(() -> new AccountNotFound("Account with ID " + accountId + " not found."));
        return new AccountDetailsResponse(
                account.id(),
                account.firstName(),
                account.lastName(),
                account.balances()
        );
    }



}
