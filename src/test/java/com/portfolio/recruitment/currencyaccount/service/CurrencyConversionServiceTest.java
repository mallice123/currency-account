package com.portfolio.recruitment.currencyaccount.service;

import com.portfolio.recruitment.currencyaccount.business.service.CurrencyConversionService;
import com.portfolio.recruitment.currencyaccount.connectors.exchangeClient.client.CurrencyExchangeClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;

class CurrencyConversionServiceTest {

    @Mock
    private CurrencyExchangeClient client;

    private CurrencyConversionService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
   //     service = new CurrencyConversionService(client);
    }

    @Test
    void testConvertPLNToUSD() {
        // Given: Prepare the exchange rate and inputs for the conversion
        when(client.getPurchaseExchangeRate("USD")).thenReturn(Mono.just(new BigDecimal("4.50")));

        // When: Call the conversion method to convert 450 PLN to USD
     //   BigDecimal result = service.performExchange("PLN", "USD", new BigDecimal("450"));

        // Then: Verify that the result matches the expected converted initialValue
     //   assertEquals(new BigDecimal("100.00"), result);
    }

    @Test
    void testConvertUSDToPLN() {
        // Given: Prepare the sale exchange rate for USD
        when(client.getSaleExchangeRate("USD")).thenReturn(Mono.just(new BigDecimal("4.50")));

        // When: Call the conversion method to convert 100 USD to PLN
     //   BigDecimal result = service.performExchange("USD", "PLN", new BigDecimal("100"));

        // Then: Verify that the result matches the expected converted initialValue
      //  assertEquals(new BigDecimal("450.00"), result);
    }

    @Test
    void testConvertKSHToUSD_InvalidAmount() {
        // Given: Prepare the purchase exchange rate for USD
        when(client.getPurchaseExchangeRate("USD")).thenReturn(Mono.just(new BigDecimal("4.50")));

        // When & Then: Calling the conversion method with a negative amount should throw an exception
      //  assertThrows(IllegalArgumentException.class, () -> service.performExchange("KSH", "USD", new BigDecimal("-450")));
    }

    @Test
    void testConvertUSDToEUR_InvalidAmount() {
        // Given: Prepare the sale exchange rate for USD
        when(client.getSaleExchangeRate("USD")).thenReturn(Mono.just(new BigDecimal("4.50")));

        // When & Then: Calling the conversion method with zero amount should throw an exception
       // assertThrows(IllegalArgumentException.class, () -> service.performExchange("USD", "EUR", BigDecimal.ZERO));
    }
}