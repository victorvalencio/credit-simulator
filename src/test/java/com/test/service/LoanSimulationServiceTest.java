package com.test.service;


import com.test.creditsimulator.dto.SimulationRequest;
import com.test.creditsimulator.messaging.MessagingService;
import com.test.creditsimulator.service.CurrencyConversionService;
import com.test.creditsimulator.service.LoanSimulationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoanSimulationServiceTest {

    @Mock
    private MessagingService messagingService;

    @Mock
    private CurrencyConversionService currencyService;

    @InjectMocks
    private LoanSimulationService simulationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSimulateWithFixedRate_Success() {
        when(currencyService.converter(any(BigDecimal.class), eq("BRL")))
                .thenReturn(BigDecimal.valueOf(10000));

        var request = new SimulationRequest(
                BigDecimal.valueOf(10000),
                LocalDate.of(1990, 5, 15),
                12,
                "FIXA",
                null,
                "BRL"
        );

        var result = simulationService.simulate(request);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(10163.24).setScale(2), result.getValorTotalPago());
        assertEquals(BigDecimal.valueOf(846.94).setScale(2), result.getParcelaMensal());
        assertEquals(BigDecimal.valueOf(163.24).setScale(2), result.getTotalJurosPago());

        verify(messagingService, times(1))
                .sendMessage(eq("simulation-results"), anyString());
    }

    @Test
    void testSimulateWithVariableRate_Success() {
        when(currencyService.converter(any(BigDecimal.class), eq("BRL")))
                .thenReturn(BigDecimal.valueOf(10000));

        var request = new SimulationRequest(
                BigDecimal.valueOf(10000),
                LocalDate.of(1990, 5, 15),
                12,
                "VARIAVEL",
                List.of(
                        new SimulationRequest.TaxaVariavel(1, BigDecimal.valueOf(0.03)),
                        new SimulationRequest.TaxaVariavel(7, BigDecimal.valueOf(0.04))
                ),
                "BRL"
        );

        var result = simulationService.simulate(request);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(10178.01).setScale(2), result.getValorTotalPago());
        assertEquals(BigDecimal.valueOf(848.17).setScale(2), result.getParcelaMensal());
        assertEquals(BigDecimal.valueOf(178.01).setScale(2), result.getTotalJurosPago());

        verify(messagingService, times(1))
                .sendMessage(eq("simulation-results"), anyString());
    }

    @Test
    void testSimulateWithCurrencyConversion() {
        when(currencyService.converter(any(BigDecimal.class), eq("USD")))
                .thenReturn(BigDecimal.valueOf(2000));

        var request = new SimulationRequest(
                BigDecimal.valueOf(10000),
                LocalDate.of(1990, 5, 15),
                12,
                "FIXA",
                null,
                "USD"
        );

        var result = simulationService.simulate(request);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(2032.65).setScale(2), result.getValorTotalPago());
        assertEquals(BigDecimal.valueOf(169.39).setScale(2), result.getParcelaMensal());
        assertEquals(BigDecimal.valueOf(32.65).setScale(2), result.getTotalJurosPago());

        verify(messagingService, times(1))
                .sendMessage(eq("simulation-results"), anyString());
    }

    @Test
    void testSimulateWithInvalidLoanAmount() {
        var request = new SimulationRequest(
                BigDecimal.valueOf(-10000),
                LocalDate.of(1990, 5, 15),
                12,
                "FIXA",
                null,
                "BRL"
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            simulationService.simulate(request);
        });

        assertEquals("O valor do empréstimo deve ser maior que zero.", exception.getMessage());
    }

    @Test
    void testSimulateWithInvalidMonths() {
        var request = new SimulationRequest(
                BigDecimal.valueOf(10000),
                LocalDate.of(1990, 5, 15),
                0,
                "FIXA",
                null,
                "BRL"
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            simulationService.simulate(request);
        });

        assertEquals("O número de meses deve ser maior que zero.", exception.getMessage());
    }

    @Test
    void testSimulateWithInvalidAge() {
        var request = new SimulationRequest(
                BigDecimal.valueOf(10000),
                LocalDate.of(2010, 5, 15),
                12,
                "FIXA",
                null,
                "BRL"
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            simulationService.simulate(request);
        });

        assertEquals("A idade do cliente deve estar entre 18 e 80 anos.", exception.getMessage());
    }
}