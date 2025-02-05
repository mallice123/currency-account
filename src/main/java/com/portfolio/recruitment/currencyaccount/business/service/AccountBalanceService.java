package com.portfolio.recruitment.currencyaccount.business.service;

import com.portfolio.recruitment.currencyaccount.business.service.model.AccountBalance;
import com.portfolio.recruitment.currencyaccount.business.service.mapper.AccountMapper;
import com.portfolio.recruitment.currencyaccount.business.service.model.Account;
import com.portfolio.recruitment.currencyaccount.connectors.db.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
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
                .filter(balance -> balance.currencyCode().getCurrencyCode().equals(currencyCode))
                .map(AccountBalance::value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Currency " + currencyCode + " not found in account balances."));
    }

    protected UpdatedBalances exchangeBalances(Account account, BigDecimal amountToConvert, BigDecimal convertedAmount,
                                            String fromCurrency, String toCurrency) {

        BigDecimal currentFromBalance = findBalanceByCurrencyCode(account, fromCurrency);
        BigDecimal currentToBalance = findBalanceByCurrencyCode(account, toCurrency);

        accountValidationService.validateSufficientFunds(currentFromBalance, fromCurrency, amountToConvert);

        BigDecimal updatedFromBalance = calculateNewBalance(currentFromBalance, amountToConvert, false);
        BigDecimal updatedToBalance = calculateNewBalance(currentToBalance, convertedAmount, true);

        updateAccountBalances(account, fromCurrency, updatedFromBalance, toCurrency, updatedToBalance);

        return new UpdatedBalances(updatedFromBalance, updatedToBalance);
    }

    private void updateAccountBalances(Account account, String fromCurrency, BigDecimal updatedFromBalance,
                                       String toCurrency, BigDecimal updatedToBalance) {
        List<AccountBalance> updatedAccountBalances = account.balances().stream()
                .map(balance -> {
                    if (balance.currencyCode().getCurrencyCode().equals(fromCurrency)) {
                        return new AccountBalance(Currency.getInstance(fromCurrency), updatedFromBalance);
                    }
                    if (balance.currencyCode().getCurrencyCode().equals(toCurrency)) {
                        return new AccountBalance(Currency.getInstance(toCurrency), updatedToBalance);
                    }
                    return balance;
                })
                .toList();

        setAccountBalances(account, updatedAccountBalances);
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

    protected record UpdatedBalances(
            BigDecimal updatedFromBalance,
            BigDecimal updatedToBalance
    )
    {
    }

}
