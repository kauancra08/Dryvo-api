package br.com.kauangoncalves.drywall_api.finance;

import java.math.BigDecimal;

public record FinanceSummaryResponse(
        int year,
        int month,
        BigDecimal grossRevenue,
        BigDecimal materialCosts,
        BigDecimal laborCosts,
        BigDecimal quoteOtherCosts,
        BigDecimal totalCosts,
        BigDecimal grossProfit,
        BigDecimal expenses,
        BigDecimal netProfit,
        long quoteCount
) {}
