package com.portfolio.recruitment.currencyaccount.business.service.model;

import java.util.List;

public record Account(
        Long id,
        String firstName,
        String lastName,
        List<AccountBalance> balances
)
{
}
