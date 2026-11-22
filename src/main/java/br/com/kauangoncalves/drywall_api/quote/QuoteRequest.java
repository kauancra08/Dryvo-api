package br.com.kauangoncalves.drywall_api.quote;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record QuoteRequest(

        @NotNull(message = "Cliente é obrigatório")
        Long clientId,

        @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
        String description,

        @DecimalMin(value = "0.00", message = "Mão de obra não pode ser negativa")
        BigDecimal laborCost,

        @DecimalMin(value = "0.00", message = "Outros custos não podem ser negativos")
        BigDecimal otherCost,

        @DecimalMin(value = "0.00", message = "Desconto não pode ser negativo")
        BigDecimal discount,

        @Valid
        List<QuoteItemRequest> items

) {
}
