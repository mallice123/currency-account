package com.portfolio.recruitment.currencyaccount.business.service;

import com.portfolio.recruitment.currencyaccount.api.dto.AccountCurrencyUpdateRequest;
import com.portfolio.recruitment.currencyaccount.business.service.model.AccountBalance;
import com.portfolio.recruitment.currencyaccount.business.service.mapper.AccountMapper;
import com.portfolio.recruitment.currencyaccount.business.service.model.Account;
import com.portfolio.recruitment.currencyaccount.business.service.model.AccountNotFound;
import com.portfolio.recruitment.currencyaccount.connectors.db.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class AccountBalanceService {

    private final AccountMapper accountMapper;
    private final AccountRepository accountRepository;
    private final AccountValidationService accountValidationService;

    public AccountBalanceService(AccountMapper accountMapper, AccountRepository accountRepository, AccountValidationService accountValidationService) {
        this.accountMapper = accountMapper;
        this.accountRepository = accountRepository;
        this.accountValidationService = accountValidationService;
    }

    protected BigDecimal calculateNewBalance(BigDecimal currentBalance, BigDecimal amount, boolean isAddition) {
        BigDecimal newBalance = isAddition
                ? currentBalance.add(amount)
                : currentBalance.subtract(amount);

        return newBalance.setScale(2, RoundingMode.HALF_UP);
    }

    protected BigDecimal findBalanceByCurrencyCode(Account account, String currencyCode) {
        return account.balances().stream()
                .filter(balance -> balance.currencyCode().equalsIgnoreCase(currencyCode))
                .map(AccountBalance::value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Currency " + currencyCode + " not found in account balances."));
    }

    @Transactional
    public void addNewBalanceType(AccountCurrencyUpdateRequest accountCurrencyUpdateRequest) {
        Account account = accountRepository.findByIdForUpdate(accountCurrencyUpdateRequest.id())
                .map(accountMapper::toAccount)
                .orElseThrow(() -> new AccountNotFound("Account with ID " + accountCurrencyUpdateRequest.id() + " not found."));

        accountValidationService.validateAvailableCurrency(accountCurrencyUpdateRequest.currencyCode());

        if(accountValidationService.validateCurrencyExists(accountCurrencyUpdateRequest, account)) {
            return;
        }

        List<AccountBalance> updatedBalances = new ArrayList<>(account.balances());
        updatedBalances.add(new AccountBalance(accountCurrencyUpdateRequest.currencyCode(),
                accountCurrencyUpdateRequest.initialValue().setScale(2, RoundingMode.HALF_UP)));

        Account updatedAccount = new Account(
                account.id(),
                account.firstName(),
                account.lastName(),
                updatedBalances
        );

        accountMapper.toAccount(
                accountRepository.save(accountMapper.toAccountEntity(updatedAccount)));

    }

    protected List<AccountBalance> exchangeBalances(Account account, BigDecimal amountToConvert, BigDecimal convertedAmount,
                                            String fromCurrency, String toCurrency) {

        BigDecimal currentFromBalance = findBalanceByCurrencyCode(account, fromCurrency);
        BigDecimal currentToBalance = findBalanceByCurrencyCode(account, toCurrency);

        accountValidationService.validateSufficientFunds(currentFromBalance, fromCurrency, amountToConvert);

        BigDecimal updatedFromBalance = calculateNewBalance(currentFromBalance, amountToConvert, false);
        BigDecimal updatedToBalance = calculateNewBalance(currentToBalance, convertedAmount, true);

      return updateAccountBalances(account, fromCurrency, updatedFromBalance, toCurrency, updatedToBalance);
    }

    private List<AccountBalance> updateAccountBalances(Account account, String fromCurrency, BigDecimal updatedFromBalance,
                                       String toCurrency, BigDecimal updatedToBalance) {
        List<AccountBalance> updatedAccountBalances = account.balances().stream()
                .map(balance -> {
                    if (balance.currencyCode().equalsIgnoreCase(fromCurrency)) {
                        return new AccountBalance(fromCurrency, updatedFromBalance);
                    }
                    if (balance.currencyCode().equalsIgnoreCase(toCurrency)) {
                        return new AccountBalance(toCurrency, updatedToBalance);
                    }
                    return balance;
                })
                .toList();
        setAccountBalances(account, updatedAccountBalances);

        return updatedAccountBalances;
    }

    private void setAccountBalances(Account account, List<AccountBalance> updatedBalances) {
        Account updatedAccount = new Account(
                account.id(),
                account.firstName(),
                account.lastName(),
                updatedBalances
        );
        accountRepository.save(accountMapper.toAccountEntity(updatedAccount));
    }

}
