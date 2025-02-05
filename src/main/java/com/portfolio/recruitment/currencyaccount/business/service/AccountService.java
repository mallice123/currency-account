package com.portfolio.recruitment.currencyaccount.business.service;

import com.portfolio.recruitment.currencyaccount.business.service.model.AccountBalance;
import com.portfolio.recruitment.currencyaccount.api.dto.AccountCreationRequest;
import com.portfolio.recruitment.currencyaccount.api.dto.AccountCurrencyUpdate;
import com.portfolio.recruitment.currencyaccount.api.dto.AccountDetailsResponse;
import com.portfolio.recruitment.currencyaccount.api.dto.AccountResponse;
import com.portfolio.recruitment.currencyaccount.business.service.mapper.AccountMapper;
import com.portfolio.recruitment.currencyaccount.business.service.model.AccountNotFound;
import com.portfolio.recruitment.currencyaccount.connectors.db.entity.AccountEntity;
import com.portfolio.recruitment.currencyaccount.business.service.model.Account;
import com.portfolio.recruitment.currencyaccount.connectors.db.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    public AccountService(AccountRepository accountRepository,
                          AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
    }

    public AccountResponse createAccount(AccountCreationRequest accountCreationRequest) {
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

    @Transactional
    public AccountDetailsResponse addNewBalanceType(AccountCurrencyUpdate accountCurrencyUpdate) {
        Account account = accountRepository.findByIdForUpdate(accountCurrencyUpdate.id())
                .map(accountMapper::toAccount)
                .orElseThrow(() -> new AccountNotFound("Account with ID " + accountCurrencyUpdate.id() + " not found."));

        if (isCurrencyExists(accountCurrencyUpdate, account)) {
            throw new IllegalArgumentException("Currency " + accountCurrencyUpdate.currencyCode() + " already exists in the account.");
        }

        List<AccountBalance> updatedBalances = new ArrayList<>(account.balances());
        updatedBalances.add(new AccountBalance(accountCurrencyUpdate.currencyCode(), accountCurrencyUpdate.value()));

        Account updatedAccount = new Account(
                account.id(),
                account.firstName(),
                account.lastName(),
                updatedBalances
        );

        Account accountAfterUpdate = accountMapper.toAccount(
                accountRepository.save(accountMapper.toAccountEntity(updatedAccount)));
        return new AccountDetailsResponse(
                accountAfterUpdate.id(),
                accountAfterUpdate.firstName(),
                accountAfterUpdate.lastName(),
                accountAfterUpdate.balances()
        );
    }

    private static boolean isCurrencyExists(AccountCurrencyUpdate accountCurrencyUpdate, Account account) {
        return account.balances().stream()
                .anyMatch(balance -> balance.currencyCode().equals(accountCurrencyUpdate.currencyCode()));
    }

}
