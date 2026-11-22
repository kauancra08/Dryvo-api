package br.com.kauangoncalves.drywall_api.quote;

import jakarta.validation.constraints.NotBlank;

public record QuoteStatusRequest(
        @NotBlank(message = "Status é obrigatório")
        String status
) {}
