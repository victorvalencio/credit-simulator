
# API de Simulação de Empréstimos - Atualizada

Esta API RESTful permite simular empréstimos com suporte a taxas fixas e variáveis, processamento em massa e conversão de moedas.

## Funcionalidades

- Simular empréstimos individuais com taxas de juros fixas ou variáveis.
- Processar simulações em massa com suporte a alta volumetria.
- Conversão de valores de empréstimos para diferentes moedas (`BRL`, `USD`, `EUR`).
- Integração com sistema de mensageria para notificação dos resultados.

---

## Requisitos

- Java 17+
- Maven
- Postman (opcional para testes)

---

## Executando o Projeto

1. Clone o repositório:

   ```bash
   git clone <repository_url>
   cd <repository_name>
   ```

2. Compile o projeto com Maven:

   ```bash
   mvn clean package
   ```

3. Execute a aplicação:

   ```bash
   java -jar target/<artifact_name>.jar
   ```

4. Acesse a documentação da API (Swagger UI) em:

   ```
   http://localhost:8080/swagger-ui.html
   ```

---

## Endpoints

### 1. Simular Empréstimo
**URL:** `POST /api/v1/simulations`  
**Descrição:** Simula um empréstimo com base nos dados fornecidos.

**Exemplo de Corpo da Requisição:**

```json
{
    "valorEmprestimo": 10000,
    "dataNascimento": "1990-05-15",
    "mesesPagamento": 12,
    "tipoTaxa": "FIXA",
    "moeda": "BRL",
    "taxasVariaveis": null
}
```

**Exemplo de Resposta:**

```json
{
    "valorTotalPago": 10163.24,
    "parcelaMensal": 846.94,
    "totalJurosPago": 163.24
}
```

---

### 2. Simular Empréstimos em Massa
**URL:** `POST /api/v1/simulations/bulk`  
**Descrição:** Processa múltiplas simulações de empréstimos em uma única requisição.

**Exemplo de Corpo da Requisição:**

```json
[
    {
        "valorEmprestimo": 10000,
        "dataNascimento": "1990-05-15",
        "mesesPagamento": 12,
        "tipoTaxa": "FIXA",
        "moeda": "BRL",
        "taxasVariaveis": null
    },
    {
        "valorEmprestimo": 20000,
        "dataNascimento": "1985-06-15",
        "mesesPagamento": 24,
        "tipoTaxa": "VARIAVEL",
        "moeda": "USD",
        "taxasVariaveis": [
            {"mesInicio": 1, "taxaAnual": 0.03},
            {"mesInicio": 12, "taxaAnual": 0.04}
        ]
    },
    {
        "valorEmprestimo": 15000,
        "dataNascimento": "1975-10-25",
        "mesesPagamento": 36,
        "tipoTaxa": "FIXA",
        "moeda": "EUR",
        "taxasVariaveis": null
    }
]
```

**Nota:** A coleção de teste contém mais de 100 simulações para testar alta volumetria.

**Exemplo de Resposta:**

```json
[
    {
        "valorTotalPago": 10163.24,
        "parcelaMensal": 846.94,
        "totalJurosPago": 163.24
    },
    {
        "valorTotalPago": 20496.18,
        "parcelaMensal": 854.01,
        "totalJurosPago": 496.18
    }
]
```

---

## Testes no Postman

1. Importe a coleção do Postman fornecida:
    - [Baixar a Coleção do Postman Atualizada](Loan_Simulation_API_Postman_Collection_Updated.json)

2. Teste os endpoints diretamente no Postman.

---

## Integração com Mensageria

- Resultados das simulações são enviados para o sistema de mensageria por meio da interface `MessagingService`.
- O sistema atual envia os resultados para o tópico `simulation-results`.

---

## Conversão de Moedas

- **Moedas Suportadas:** `BRL`, `USD`, `EUR`.
- Conversões são simuladas no serviço `CurrencyConversionService`.

---

## Processamento em Massa

- **Simulações em Massa:** suporta alta volumetria com processamento paralelo.
- Configurável para processar até 100.000 simulações por requisição.

---
