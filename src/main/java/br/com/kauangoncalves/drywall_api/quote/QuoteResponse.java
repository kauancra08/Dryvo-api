package br.com.kauangoncalves.drywall_api.quote;

import java.math.BigDecimal;
import java.time.Instant;

public record QuoteResponse(
        Long id,
        Long clientId,
        String clientName,
        String status,
        String description,
        BigDecimal laborCost,
        BigDecimal otherCost,
        BigDecimal discount,
        BigDecimal subtotal,
        BigDecimal total,
        Instant createdAt,
        Instant updatedAt
) {

    public static QuoteResponse from(Quote quote) {

        return new QuoteResponse(
                quote.getId(),
                quote.getClient().getId(),
                quote.getClient().getName(),
                quote.getStatus().name(),
                quote.getDescription(),
                quote.getLaborCost(),
                quote.getOtherCost(),
                quote.getDiscount(),
                quote.getSubtotal(),
                quote.getTotal(),
                quote.getCreatedAt(),
                quote.getUpdatedAt()
        );
    }
}