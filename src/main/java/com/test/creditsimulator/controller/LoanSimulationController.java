package com.test.creditsimulator.controller;

import com.test.creditsimulator.dto.SimulationRequest;
import com.test.creditsimulator.dto.SimulationResult;
import com.test.creditsimulator.service.LoanSimulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/simulations")
public class LoanSimulationController {

    private final LoanSimulationService simulationService;

    public LoanSimulationController(LoanSimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @Operation(summary = "Simula um empréstimo com valor, idade e prazo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Simulação bem-sucedida"),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos dados de entrada")
    })
    @PostMapping
    public SimulationResult simulateLoan(@RequestBody @Valid SimulationRequest request) {
        return simulationService.simulate(request);
    }

    @Operation(summary = "Simula múltiplos empréstimos em uma única requisição")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Simulações bem-sucedidas"),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos dados de entrada")
    })
    @PostMapping("/bulk")
    public List<SimulationResult> bulkSimulate(@RequestBody List<SimulationRequest> requests) {
        return simulationService.bulkSimulate(requests);
    }
}