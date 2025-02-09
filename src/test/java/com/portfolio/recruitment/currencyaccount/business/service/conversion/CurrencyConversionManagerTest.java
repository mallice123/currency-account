package com.portfolio.recruitment.currencyaccount.business.service.conversion;

import com.portfolio.recruitment.currencyaccount.business.service.conversion.strategies.ForeignCurrencyToPLNStrategy;
import com.portfolio.recruitment.currencyaccount.business.service.conversion.strategies.PLNToForeignCurrencyStrategy;
import com.portfolio.recruitment.currencyaccount.connectors.exchangeClient.client.CurrencyExchangeClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

import static com.portfolio.recruitment.currencyaccount.business.service.util.CurrencyConstants.CURRENCY_CODE_EUR;
import static com.portfolio.recruitment.currencyaccount.business.service.util.CurrencyConstants.CURRENCY_CODE_PLN;
import static com.portfolio.recruitment.currencyaccount.business.service.util.CurrencyConstants.CURRENCY_CODE_USD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class CurrencyConversionManagerTest {

    @InjectMocks
    private CurrencyConversionManager manager;

    @Mock
    private PLNToForeignCurrencyStrategy plnToForeignCurrencyStrategy;

    @Mock
    private ForeignCurrencyToPLNStrategy foreignCurrencyToPLNStrategy;

    @Mock
    private CurrencyExchangeClient exchangeClient;

    @BeforeEach
    public void setUp() {
        manager = new CurrencyConversionManager(List.of(plnToForeignCurrencyStrategy, foreignCurrencyToPLNStrategy));
    }

    @Test
    void testConvertPLNToForeignCurrency() {
        when(plnToForeignCurrencyStrategy.supports(CURRENCY_CODE_PLN, CURRENCY_CODE_USD)).thenReturn(true);
        when(exchangeClient.getPurchaseExchangeRate(CURRENCY_CODE_USD)).thenReturn(Mono.just(new BigDecimal("4.50")));

        when(plnToForeignCurrencyStrategy.convert(BigDecimal.valueOf(1000), CURRENCY_CODE_PLN, CURRENCY_CODE_USD, exchangeClient))
                .thenCallRealMethod();

        BigDecimal result = manager.convert(BigDecimal.valueOf(1000), CURRENCY_CODE_PLN, CURRENCY_CODE_USD, exchangeClient);

        assertEquals(new BigDecimal("222.22"), result);
    }

    @Test
    void testConvertForeignCurrencyToPLN() {
        when(foreignCurrencyToPLNStrategy.supports(CURRENCY_CODE_USD, CURRENCY_CODE_PLN)).thenReturn(true);
        when(exchangeClient.getSaleExchangeRate(CURRENCY_CODE_USD)).thenReturn(Mono.just(new BigDecimal("4.11")));

        when(foreignCurrencyToPLNStrategy.convert(BigDecimal.valueOf(1000), CURRENCY_CODE_USD, CURRENCY_CODE_PLN, exchangeClient))
                .thenCallRealMethod();

        BigDecimal result = manager.convert(BigDecimal.valueOf(1000),CURRENCY_CODE_USD, CURRENCY_CODE_PLN, exchangeClient);
        assertEquals(new BigDecimal("4110.00"), result);
    }

    @Test
    void testStrategyNotAvailable() {
        when(plnToForeignCurrencyStrategy.supports(CURRENCY_CODE_EUR, CURRENCY_CODE_USD)).thenReturn(false);
        when(foreignCurrencyToPLNStrategy.supports(CURRENCY_CODE_EUR, CURRENCY_CODE_USD)).thenReturn(false);

        assertThrows(UnsupportedOperationException.class, () ->
                manager.convert(BigDecimal.valueOf(500), CURRENCY_CODE_EUR, CURRENCY_CODE_USD, exchangeClient)
        );
        assertThrows(UnsupportedOperationException.class, () ->
                manager.convert(BigDecimal.valueOf(300),CURRENCY_CODE_PLN , CURRENCY_CODE_USD, exchangeClient)
        );
    }

}