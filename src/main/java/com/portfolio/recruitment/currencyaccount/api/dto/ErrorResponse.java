package com.portfolio.recruitment.currencyaccount.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        int status,
        String message,
        String errorCode,
        LocalDateTime timestamp,
        List<String> details
)
{
}
