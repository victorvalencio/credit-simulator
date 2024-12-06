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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class LoanSimulationServicePerformanceTest {
    private final LoanSimulationService service = new LoanSimulationService();

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
    void testBulkSimulate() {
        when(currencyService.converter(any(BigDecimal.class), eq("BRL")))
                .thenReturn(BigDecimal.valueOf(10000));

        var requests = List.of(
                new SimulationRequest(
                        BigDecimal.valueOf(10000),
                        LocalDate.of(1990, 5, 15),
                        12,
                        "FIXA",
                        null,
                        "BRL"
                ),
                new SimulationRequest(
                        BigDecimal.valueOf(20000),
                        LocalDate.of(1985, 6, 15),
                        24,
                        "VARIAVEL",
                        List.of(
                                new SimulationRequest.TaxaVariavel(1, BigDecimal.valueOf(0.03)),
                                new SimulationRequest.TaxaVariavel(12, BigDecimal.valueOf(0.04))
                        ),
                        "BRL"
                )
        );

        var results = simulationService.bulkSimulate(requests);

        assertNotNull(results); // Verifica que a lista de resultados não é nula
        assertEquals(2, results.size()); // Verifica que duas simulações foram processadas

        // Verifica que o serviço de mensageria foi chamado para cada simulação
        verify(messagingService, times(2))
                .sendMessage(eq("simulation-results"), anyString());
    }
}
