package com.test.creditsimulator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class SimulationResult {
    private BigDecimal valorTotalPago;
    private BigDecimal parcelaMensal;
    private BigDecimal totalJurosPago;
}