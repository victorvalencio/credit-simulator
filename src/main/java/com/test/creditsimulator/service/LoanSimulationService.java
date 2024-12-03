package com.test.creditsimulator.service;

import com.test.creditsimulator.dto.SimulationRequest;
import com.test.creditsimulator.dto.SimulationResult;
import com.test.creditsimulator.messaging.MessagingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanSimulationService {

    @Autowired
    private MessagingService messagingService;

    public SimulationResult simulate(SimulationRequest request) {
        var valorEmprestimo = request.getValorEmprestimo();
        if (valorEmprestimo.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do empréstimo deve ser maior que zero.");
        }

        int meses = request.getMesesPagamento();
        if (meses <= 0) {
            throw new IllegalArgumentException("O número de meses deve ser maior que zero.");
        }

        var taxaAnual = determinarTaxaJuros(calcularIdade(request.getDataNascimento()));
        var taxaMensal = taxaAnual.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_EVEN);

        var parcelaMensal = calcularParcela(valorEmprestimo, taxaMensal, meses);

        var valorTotalPago = parcelaMensal.multiply(BigDecimal.valueOf(meses));

        var totalJurosPago = valorTotalPago.subtract(valorEmprestimo);

        return new SimulationResult(valorTotalPago, parcelaMensal, totalJurosPago);
    }

    public List<SimulationResult> bulkSimulate(List<SimulationRequest> requests) {
        return requests.parallelStream()
                .map(this::simulate)
                .collect(Collectors.toList());
    }

    private int calcularIdade(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private BigDecimal determinarTaxaJuros(int age) {
        if (age <= 25) return BigDecimal.valueOf(0.05);
        else if (age <= 40) return BigDecimal.valueOf(0.03);
        else if (age <= 60) return BigDecimal.valueOf(0.02);
        else return BigDecimal.valueOf(0.04);
    }

    private BigDecimal calcularParcela(BigDecimal valor, BigDecimal taxa, int meses) {
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do empréstimo deve ser maior que zero.");
        }
        if (meses <= 0) {
            throw new IllegalArgumentException("O número de meses deve ser maior que zero.");
        }
        if (taxa.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("A taxa de juros não pode ser negativa.");
        }

        if (taxa.compareTo(BigDecimal.ZERO) == 0) {
            return valor.divide(BigDecimal.valueOf(meses), RoundingMode.HALF_EVEN);
        }

        var base = BigDecimal.ONE.add(taxa);
        var basePowMeses = base.pow(meses, new MathContext(15, RoundingMode.HALF_EVEN));
        var numerador = valor.multiply(taxa);
        var denominador = BigDecimal.ONE.subtract(BigDecimal.ONE.divide(basePowMeses, 10, RoundingMode.HALF_EVEN));

        if (denominador.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Erro no cálculo da parcela: denominador igual a zero.");
        }

        return numerador.divide(denominador, 10, RoundingMode.HALF_EVEN);
    }
}