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

    @Autowired
    private CurrencyConversionService currencyService;

    public SimulationResult simulate(SimulationRequest request) {
        var valorEmprestimo = request.getValorEmprestimo();
        if (valorEmprestimo.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do empréstimo deve ser maior que zero.");
        }

        int meses = request.getMesesPagamento();
        if (meses <= 0) {
            throw new IllegalArgumentException("O número de meses deve ser maior que zero.");
        }

        int idade = calcularIdade(request.getDataNascimento());
        if (idade < 18 || idade > 80) {
            throw new IllegalArgumentException("A idade do cliente deve estar entre 18 e 80 anos.");
        }

        var moeda = request.getMoeda();
        if (moeda == null || moeda.isEmpty()) {
            throw new IllegalArgumentException("A moeda é obrigatória.");
        }

        if ("VARIAVEL".equalsIgnoreCase(request.getTipoTaxa()) &&
                (request.getTaxasVariaveis() == null || request.getTaxasVariaveis().isEmpty())) {
            throw new IllegalArgumentException("As taxas variáveis devem ser fornecidas para o tipo VARIAVEL.");
        }

        BigDecimal valorTotalPago;
        BigDecimal parcelaMensal;
        BigDecimal totalJurosPago;

        var valorConvertido = currencyService.converter(valorEmprestimo, moeda);

        if ("FIXA".equalsIgnoreCase(request.getTipoTaxa())) {
            var taxaAnual = determinarTaxaJuros(idade);
            var taxaMensal = taxaAnual.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_EVEN);

            parcelaMensal = calcularParcela(valorConvertido, taxaMensal, meses);
            valorTotalPago = parcelaMensal.multiply(BigDecimal.valueOf(meses));
            totalJurosPago = valorTotalPago.subtract(valorConvertido);

        } else if ("VARIAVEL".equalsIgnoreCase(request.getTipoTaxa())) {
            valorTotalPago = calcularComTaxaVariavel(valorConvertido, meses, request.getTaxasVariaveis());
            parcelaMensal = valorTotalPago.divide(BigDecimal.valueOf(meses), 2, RoundingMode.HALF_EVEN);
            totalJurosPago = valorTotalPago.subtract(valorConvertido);
        } else {
            throw new IllegalArgumentException("Tipo de taxa inválido.");
        }

        var result = new SimulationResult(
                valorTotalPago.setScale(2, RoundingMode.HALF_EVEN),
                parcelaMensal.setScale(2, RoundingMode.HALF_EVEN),
                totalJurosPago.setScale(2, RoundingMode.HALF_EVEN)
        );

        messagingService.sendMessage(
                "simulation-results",
                "Resultado da simulação: " + result
        );

        return result;
    }

    public List<SimulationResult> bulkSimulate(List<SimulationRequest> requests) {
        return requests.parallelStream()
                .map(this::simulate)
                .collect(Collectors.toList());
    }

    private int calcularIdade(LocalDate dataNascimento) {
        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }

    private BigDecimal determinarTaxaJuros(int age) {
        return getDecimal(age);
    }

    private static BigDecimal getDecimal(int age) {
        if (age <= 25) return BigDecimal.valueOf(0.05);
        else if (age <= 40) return BigDecimal.valueOf(0.03);
        else if (age <= 60) return BigDecimal.valueOf(0.02);
        else return BigDecimal.valueOf(0.04);
    }

    private BigDecimal calcularParcela(BigDecimal valor, BigDecimal taxa, int meses) {
        BigDecimal valor1 = getBigDecimal(valor, taxa, meses);
        if (valor1 != null) return valor1;

        var base = BigDecimal.ONE.add(taxa);
        var basePowMeses = base.pow(meses, new MathContext(15, RoundingMode.HALF_EVEN));
        var numerador = valor.multiply(taxa);
        var denominador = BigDecimal.ONE.subtract(BigDecimal.ONE.divide(basePowMeses, 10, RoundingMode.HALF_EVEN));

        if (denominador.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Erro no cálculo da parcela: denominador igual a zero.");
        }

        return numerador.divide(denominador, 10, RoundingMode.HALF_EVEN);
    }

    private static BigDecimal getBigDecimal(BigDecimal valor, BigDecimal taxa, int meses) {
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
            return valor.divide(BigDecimal.valueOf(meses), 10, RoundingMode.HALF_EVEN);
        }
        return null;
    }

    private BigDecimal calcularComTaxaVariavel(BigDecimal valor, int meses, List<SimulationRequest.TaxaVariavel> taxas) {
        if (taxas == null || taxas.isEmpty()) {
            throw new IllegalArgumentException("As taxas variáveis devem ser fornecidas para o tipo VARIAVEL.");
        }

        BigDecimal valorRestante = valor;
        BigDecimal valorTotal = BigDecimal.ZERO;

        for (int i = 1; i <= meses; i++) {
            BigDecimal taxaAnual = obterTaxaParaMes(taxas, i);
            BigDecimal taxaMensal = taxaAnual.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_EVEN);

            BigDecimal juros = valorRestante.multiply(taxaMensal);
            BigDecimal principal = calcularParcela(valorRestante, taxaMensal, meses - i + 1).subtract(juros);

            valorTotal = valorTotal.add(juros.add(principal));
            valorRestante = valorRestante.subtract(principal);
        }

        return valorTotal;
    }

    private BigDecimal obterTaxaParaMes(List<SimulationRequest.TaxaVariavel> taxas, int mes) {
        return taxas.stream()
                .filter(t -> t.getMesInicio() <= mes)
                .map(SimulationRequest.TaxaVariavel::getTaxaAnual)
                .reduce((first, second) -> second) // Última taxa aplicável ao mês
                .orElseThrow(() -> new IllegalArgumentException("Taxa não encontrada para o mês: " + mes));
    }
}
