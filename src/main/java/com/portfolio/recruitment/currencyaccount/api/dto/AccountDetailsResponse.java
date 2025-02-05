package com.portfolio.recruitment.currencyaccount.api.dto;

import com.portfolio.recruitment.currencyaccount.business.service.model.AccountBalance;

import java.util.List;

public record AccountDetailsResponse(
        long id,
        String firstName,
        String lastName,
        List<AccountBalance> currentBalance
)
{
}
