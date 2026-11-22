package br.com.kauangoncalves.drywall_api.quote;

import java.util.List;

public record QuoteDetailsResponse(
        QuoteResponse quote,
        List<QuoteItemResponse> items
) {}
