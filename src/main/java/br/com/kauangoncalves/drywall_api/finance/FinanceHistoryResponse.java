package br.com.kauangoncalves.drywall_api.finance;

import java.math.BigDecimal;

public record FinanceHistoryResponse(
        int year,
        int month,
        BigDecimal grossRevenue,
        BigDecimal totalCosts,
        BigDecimal grossProfit,
        BigDecimal expenses,
        BigDecimal netProfit,
        long quoteCount
) {}
