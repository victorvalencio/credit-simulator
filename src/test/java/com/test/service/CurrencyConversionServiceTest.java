package com.test.service;

import com.test.creditsimulator.service.CurrencyConversionService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrencyConversionServiceTest {

    private final CurrencyConversionService currencyService = new CurrencyConversionService();

    @Test
    void testConvertToUSD() {
        var valorBRL = BigDecimal.valueOf(10000); // R$ 10.000,00
        var moeda = "USD";

        var valorConvertido = currencyService.converter(valorBRL, moeda);

        // Supondo que 1 BRL = 0.2 USD
        assertEquals(0, BigDecimal.valueOf(2000.00).setScale(2).compareTo(valorConvertido));
    }

    @Test
    void testConvertToEUR() {
        var valorBRL = BigDecimal.valueOf(10000); // R$ 10.000,00
        var moeda = "EUR";

        var valorConvertido = currencyService.converter(valorBRL, moeda);

        assertEquals(BigDecimal.valueOf(1800.00).setScale(2), valorConvertido);
    }

    @Test
    void testConvertInvalidCurrency() {
        var valorBRL = BigDecimal.valueOf(10000); // R$ 10.000,00
        var moeda = "INVALID";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            currencyService.converter(valorBRL, moeda);
        });

        assertEquals("Moeda não suportada: INVALID", exception.getMessage());
    }
}