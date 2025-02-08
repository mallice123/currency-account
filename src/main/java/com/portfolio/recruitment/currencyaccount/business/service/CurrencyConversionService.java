package com.portfolio.recruitment.currencyaccount.business.service;

import com.portfolio.recruitment.currencyaccount.api.dto.ConversionResponse;
import com.portfolio.recruitment.currencyaccount.api.dto.ExchangeRequest;
import com.portfolio.recruitment.currencyaccount.business.service.conversion.CurrencyConversionManager;
import com.portfolio.recruitment.currencyaccount.business.service.mapper.AccountMapper;
import com.portfolio.recruitment.currencyaccount.business.service.model.Account;
import com.portfolio.recruitment.currencyaccount.business.service.model.AccountNotFound;
import com.portfolio.recruitment.currencyaccount.connectors.db.repository.AccountRepository;
import com.portfolio.recruitment.currencyaccount.connectors.exchangeClient.client.CurrencyExchangeClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CurrencyConversionService {

    private final CurrencyExchangeClient currencyExchangeClient;
    private final AccountBalanceService accountBalanceService;
    private final AccountValidationService accountValidationService;
    private final AccountMapper accountMapper;
    private final AccountRepository accountRepository;
    private final CurrencyConversionManager currencyConversionManager;

    public CurrencyConversionService(CurrencyExchangeClient currencyExchangeClient, AccountBalanceService accountBalanceService,
                                     AccountValidationService accountValidationService, AccountMapper accountMapper, AccountRepository accountRepository, CurrencyConversionManager currencyConversionManager) {
        this.currencyExchangeClient = currencyExchangeClient;
        this.accountBalanceService = accountBalanceService;
        this.accountValidationService = accountValidationService;
        this.accountMapper = accountMapper;
        this.accountRepository = accountRepository;
        this.currencyConversionManager = currencyConversionManager;
    }

    @Transactional
    public ConversionResponse exchangeCurrency(ExchangeRequest exchangeRequest) {
        accountValidationService.validateAvailableCurrency(exchangeRequest.initialCurrency(), exchangeRequest.targetCurrency());
        Account account = accountMapper.toAccount(accountRepository.findByIdForUpdate(exchangeRequest.id()).
                orElseThrow(() -> new AccountNotFound("Account with ID " + exchangeRequest.id() + " not found.")));

        return performExchange(exchangeRequest, account);
    }

    private ConversionResponse performExchange(ExchangeRequest exchangeRequest, Account account) {
        String fromCurrency = exchangeRequest.initialCurrency();
        String toCurrency = exchangeRequest.targetCurrency();
        BigDecimal amountToConvert = exchangeRequest.amount();

        BigDecimal currentFromBalance = accountBalanceService.findBalanceByCurrencyCode(account, fromCurrency);

        accountValidationService.validateSufficientFunds(currentFromBalance, exchangeRequest.initialCurrency(), amountToConvert);

        BigDecimal convertedAmount = currencyConversionManager.convert(amountToConvert, fromCurrency, toCurrency, currencyExchangeClient);

        return new ConversionResponse(
                accountBalanceService.exchangeBalances(
                        account, amountToConvert, convertedAmount, fromCurrency, toCurrency
                )
        );
    }

}
