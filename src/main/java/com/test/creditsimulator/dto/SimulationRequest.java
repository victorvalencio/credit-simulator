package com.test.creditsimulator.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimulationRequest {

    @NotNull(message = "O valor do empréstimo é obrigatório.")
    @DecimalMin(value = "0.01", inclusive = true, message = "O valor do empréstimo deve ser maior que zero.")
    @DecimalMax(value = "1000000", inclusive = true, message = "O valor do empréstimo não pode exceder R$ 1.000.000,00.")
    private BigDecimal valorEmprestimo;

    @NotNull(message = "A data de nascimento é obrigatória.")
    @Past(message = "A data de nascimento deve estar no passado.")
    private LocalDate dataNascimento;

    @NotNull(message = "O prazo de pagamento é obrigatório.")
    @Min(value = 1, message = "O prazo de pagamento deve ser de pelo menos 1 mês.")
    @Max(value = 360, message = "O prazo de pagamento não pode exceder 360 meses.")
    private Integer mesesPagamento;

    @AssertTrue(message = "A idade do cliente deve estar entre 18 e 80 anos.")
    public boolean isIdadeValida() {
        if (dataNascimento == null) {
            return false;
        }
        int idade = Period.between(dataNascimento, LocalDate.now()).getYears();
        return idade >= 18 && idade <= 80;
    }

    @NotNull(message = "O tipo de taxa de juros é obrigatório.")
    @Pattern(regexp = "FIXA|VARIAVEL", message = "O tipo de taxa deve ser FIXA ou VARIAVEL.")
    private String tipoTaxa;

    private List<TaxaVariavel> taxasVariaveis;

    @NotNull(message = "A moeda é obrigatória.")
    @Pattern(regexp = "BRL|USD|EUR", message = "Moeda suportada: BRL, USD ou EUR.")
    private String moeda;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TaxaVariavel {
        @NotNull(message = "O mês de início é obrigatório.")
        @Min(value = 1, message = "O mês de início deve ser maior ou igual a 1.")
        private Integer mesInicio;

        @NotNull(message = "A taxa anual é obrigatória.")
        @DecimalMin(value = "0.00", inclusive = true, message = "A taxa anual deve ser maior ou igual a 0.")
        @DecimalMax(value = "1.00", inclusive = true, message = "A taxa anual deve ser menor ou igual a 1 (100%).")
        private BigDecimal taxaAnual;
    }
}