package com.test.creditsimulator.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class CurrencyConversionService {

    private static final Map<String, BigDecimal> TAXAS_CAMBIO = Map.of(
            "BRL", BigDecimal.ONE,            // 1 BRL = 1 BRL
            "USD", BigDecimal.valueOf(0.20),  // 1 BRL = 0.20 USD
            "EUR", BigDecimal.valueOf(0.18)   // 1 BRL = 0.18 EUR
    );

    public BigDecimal converter(BigDecimal valor, String moedaDestino) {
        if (!TAXAS_CAMBIO.containsKey(moedaDestino)) {
            throw new IllegalArgumentException("Moeda não suportada: " + moedaDestino);
        }
        return valor.multiply(TAXAS_CAMBIO.get(moedaDestino));
    }

    public BigDecimal obterTaxa(String moeda) {
        if (!TAXAS_CAMBIO.containsKey(moeda)) {
            throw new IllegalArgumentException("Moeda não suportada: " + moeda);
        }
        return TAXAS_CAMBIO.get(moeda);
    }
}
