package com.test.service;


import com.test.creditsimulator.dto.SimulationRequest;
import com.test.creditsimulator.dto.SimulationResult;
import com.test.creditsimulator.service.LoanSimulationService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LoanSimulationServiceTest {

    private final LoanSimulationService service = new LoanSimulationService();

    @Test
    void testSimular_CalculoCorreto() {
        var requisicao = new SimulationRequest(
                BigDecimal.valueOf(10000),
                LocalDate.of(1990, 5, 15),
                12
        );

        var resultado = service.simulate(requisicao);

        assertNotNull(resultado);
        assertEquals(BigDecimal.valueOf(10272.84).setScale(2), resultado.getValorTotalPago().setScale(2));
        assertEquals(BigDecimal.valueOf(856.07).setScale(2), resultado.getParcelaMensal().setScale(2));
        assertEquals(BigDecimal.valueOf(272.84).setScale(2), resultado.getTotalJurosPago().setScale(2));
    }

    @Test
    void testSimular_IdadeInvalida() {
        var requisicao = new SimulationRequest(
                BigDecimal.valueOf(10000),
                LocalDate.of(2010, 5, 15), // Idade menor que 18
                12
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.simulate(requisicao);
        });

        assertEquals("A idade do cliente deve estar entre 18 e 80 anos.", exception.getMessage());
    }
}
