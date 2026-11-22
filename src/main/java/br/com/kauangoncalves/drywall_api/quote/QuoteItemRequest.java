package br.com.kauangoncalves.drywall_api.quote;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record QuoteItemRequest(

        Long materialId,

        @NotBlank(message = "Descrição do item é obrigatória")
        String description,

        @NotBlank(message = "Unidade é obrigatória")
        String unit,

        @NotNull(message = "Quantidade é obrigatória")
        @Positive(message = "Quantidade deve ser maior que zero")
        BigDecimal quantity,

        @NotNull(message = "Preço unitário é obrigatório")
        @DecimalMin(value = "0.00", message = "Preço unitário não pode ser negativo")
        BigDecimal unitPrice

) {
}