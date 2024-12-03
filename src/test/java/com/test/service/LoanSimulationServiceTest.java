package com.test.service;


import com.test.creditsimulator.dto.SimulationRequest;
import com.test.creditsimulator.service.LoanSimulationService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        assertEquals(BigDecimal.valueOf(10163.24).setScale(2, RoundingMode.HALF_EVEN), resultado.getValorTotalPago());
        assertEquals(BigDecimal.valueOf(846.94).setScale(2, RoundingMode.HALF_EVEN), resultado.getParcelaMensal());
        assertEquals(BigDecimal.valueOf(163.24).setScale(2, RoundingMode.HALF_EVEN), resultado.getTotalJurosPago());
    }

    @Test
    void testSimular_IdadeInvalida() {
        var requisicao = new SimulationRequest(
                BigDecimal.valueOf(10000),
                LocalDate.of(2010, 5, 15),
                12
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.simulate(requisicao);
        });

        assertEquals("A idade do cliente deve estar entre 18 e 80 anos.", exception.getMessage());
    }

    @Test
    void testSimular_ValorEmprestimoInvalido() {
        var requisicao = new SimulationRequest(
                BigDecimal.valueOf(-10000),
                LocalDate.of(1990, 5, 15),
                12
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.simulate(requisicao);
        });

        assertEquals("O valor do empréstimo deve ser maior que zero.", exception.getMessage());
    }

    @Test
    void testIsIdadeValida_Valida() {
        var requisicao = new SimulationRequest(
                BigDecimal.valueOf(10000),
                LocalDate.of(1990, 5, 15),
                12
        );

        assertTrue(requisicao.isIdadeValida());
    }

    @Test
    void testIsIdadeValida_Invalida() {
        SimulationRequest requisicao = new SimulationRequest(
                BigDecimal.valueOf(10000),
                LocalDate.of(2010, 5, 15),
                12
        );

        assertFalse(requisicao.isIdadeValida());
    }

}
