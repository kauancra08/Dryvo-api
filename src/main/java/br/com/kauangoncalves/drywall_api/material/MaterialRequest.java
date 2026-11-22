package br.com.kauangoncalves.drywall_api.material;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MaterialRequest(

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
        String name,

        @Size(max = 60, message = "Categoria deve ter no máximo 60 caracteres")
        String category,

        @NotBlank(message = "Unidade é obrigatória")
        @Size(max = 20, message = "Unidade deve ter no máximo 20 caracteres")
        String unit,

        @NotNull(message = "Preço unitário é obrigatório")
        @DecimalMin(value = "0.00", message = "Preço não pode ser negativo")
        BigDecimal unitPrice,

        @Size(max = 150, message = "Fornecedor deve ter no máximo 150 caracteres")
        String supplier,

        @Size(max = 2000, message = "Observações devem ter no máximo 2000 caracteres")
        String notes
) {}