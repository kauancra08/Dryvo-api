package br.com.kauangoncalves.drywall_api.finance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinanceExpenseRequest(
        @NotBlank(message = "Descrição é obrigatória")
        @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
        String description,

        @Size(max = 100, message = "Categoria deve ter no máximo 100 caracteres")
        String category,

        @NotNull(message = "Valor é obrigatório")
        @Positive(message = "Valor deve ser maior que zero")
        BigDecimal amount,

        @NotNull(message = "Data é obrigatória")
        LocalDate date,

        @Size(max = 5000, message = "Observações devem ter no máximo 5000 caracteres")
        String notes
) {}
