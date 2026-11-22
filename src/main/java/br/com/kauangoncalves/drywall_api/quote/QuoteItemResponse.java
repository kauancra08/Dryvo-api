package br.com.kauangoncalves.drywall_api.quote;

import java.math.BigDecimal;

public record QuoteItemResponse(
        Long id,
        Long materialId,
        String description,
        String unit,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal total
) {
    public static QuoteItemResponse from(QuoteItem item) {
        return new QuoteItemResponse(
                item.getId(),
                item.getMaterial() == null ? null : item.getMaterial().getId(),
                item.getDescription(),
                item.getUnit(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotal()
        );
    }
}
