package com.test.service;

import com.test.creditsimulator.dto.SimulationRequest;
import com.test.creditsimulator.service.LoanSimulationService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoanSimulationServicePerformanceTest {
    private final LoanSimulationService service = new LoanSimulationService();

    @Test
    void testSimularAltaVolumetria() {
        List<SimulationRequest> requisicoes = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            requisicoes.add(new SimulationRequest(
                    BigDecimal.valueOf(100000 + i),
                    LocalDate.of(1990, 5, 15),
                    12
            ));
        }

        long startTime = System.currentTimeMillis();
        requisicoes.parallelStream().forEach(service::simulate);
        long endTime = System.currentTimeMillis();

        System.out.println("Tempo para processar 100.000 simulações: " + (endTime - startTime) + "ms");

        assertEquals(100000, requisicoes.size());
    }
}

