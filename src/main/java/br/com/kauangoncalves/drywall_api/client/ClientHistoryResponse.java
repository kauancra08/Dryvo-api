package br.com.kauangoncalves.drywall_api.client;

import br.com.kauangoncalves.drywall_api.quote.QuoteResponse;

import java.math.BigDecimal;
import java.util.List;

public record ClientHistoryResponse(
        Long clientId,
        String clientName,
        BigDecimal totalQuoted,
        BigDecimal totalRealized,
        BigDecimal totalPending,
        BigDecimal totalRejected,
        List<QuoteResponse> quotes
) {}
