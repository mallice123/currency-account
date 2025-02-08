package com.portfolio.recruitment.currencyaccount.business.service.mapper;

import com.portfolio.recruitment.currencyaccount.business.service.model.AccountBalance;
import com.portfolio.recruitment.currencyaccount.business.service.model.Account;
import com.portfolio.recruitment.currencyaccount.connectors.db.entity.AccountEntity;
import com.portfolio.recruitment.currencyaccount.connectors.db.entity.BalanceEntity;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AccountMapper {

    public Account toAccount(AccountEntity accountEntity) {
        return new Account(
                accountEntity.getId(),
                accountEntity.getFirstName(),
                accountEntity.getLastName(),
                toAccountBalanceList(accountEntity.getBalances())
        );
    }

    public AccountEntity toAccountEntity(Account account) {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setId(account.id());
        accountEntity.setFirstName(account.firstName());
        accountEntity.setLastName(account.lastName());
        accountEntity.setBalances(toBalanceEntityList(account.balances(), account.id()));

        return accountEntity;
    }

    private AccountBalance toAccountBalance(BalanceEntity balanceEntity) {
        return new AccountBalance(
                balanceEntity.getCurrencyCode(),
                balanceEntity.getValue()
        );
    }

    private BalanceEntity toBalanceEntity(AccountBalance balance, Long accountId) {
        BalanceEntity balanceEntity = new BalanceEntity();
        balanceEntity.setAccountId(accountId);
        balanceEntity.setCurrencyCode(balance.currencyCode());
        balanceEntity.setValue(balance.value());

        return balanceEntity;
    }

    private List<AccountBalance> toAccountBalanceList(List<BalanceEntity> balanceEntities) {
        return balanceEntities.stream()
                .map(this::toAccountBalance)
                .collect(Collectors.toList());
    }

    private List<BalanceEntity> toBalanceEntityList(List<AccountBalance> balances, Long accountId) {
        return balances.stream()
                .map(balance -> toBalanceEntity(balance, accountId))
                .collect(Collectors.toList());
    }

}