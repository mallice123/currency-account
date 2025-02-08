package com.portfolio.recruitment.currencyaccount.api;

import com.portfolio.recruitment.currencyaccount.api.dto.ErrorResponse;
import com.portfolio.recruitment.currencyaccount.business.service.model.AccountNotFound;
import com.portfolio.recruitment.currencyaccount.business.service.model.CurrencyNotSupported;
import com.portfolio.recruitment.currencyaccount.business.service.model.ExchangeRateNotAvailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                errorMessage,
                LocalDateTime.now(),
                List.of("Validation of failed")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        String errorMessage = e.getMessage();
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "ILLEGAL_ARGUMENT",
                errorMessage,
                LocalDateTime.now(),
                List.of("Provided argument is invalid"
        ));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ExchangeRateNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleExchangeRateNotAvailable(ExchangeRateNotAvailableException e) {
        String errorMessage = e.getMessage();
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "EXCHANGE_RATE_UNAVAILABLE",
                errorMessage,
                LocalDateTime.now(),
                List.of("Exchange rate not available")
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        String errorMessage = e.getMessage();
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_ERROR",
                errorMessage,
                LocalDateTime.now(),
                List.of("An unexpected error occurred.")
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(AccountNotFound.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFound(AccountNotFound e) {
        String errorMessage = e.getMessage();
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "ACCOUNT_NOT_FOUND",
                errorMessage,
                LocalDateTime.now(),
                List.of("Account could not be found")
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(CurrencyNotSupported.class)
    public ResponseEntity<ErrorResponse> handleCurrencyNotSupported(CurrencyNotSupported e) {
        String errorMessage = e.getMessage();
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "CURRENCY_NOT_SUPPORTED",
                errorMessage,
                LocalDateTime.now(),
                List.of("Currency not supported")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ErrorResponse> handleCurrencyNotSupported(UnsupportedOperationException e) {
        String errorMessage = e.getMessage();
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "STRATEGY_NOT_FOUND",
                errorMessage,
                LocalDateTime.now(),
                List.of("No strategy available for this operation")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}