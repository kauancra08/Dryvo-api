package br.com.kauangoncalves.drywall_api.finance;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinanceExpenseResponse(
        Long id,
        String description,
        String category,
        BigDecimal amount,
        LocalDate date,
        String notes
) {
    public static FinanceExpenseResponse from(FinanceExpense expense) {
        return new FinanceExpenseResponse(
                expense.getId(),
                expense.getDescription(),
                expense.getCategory(),
                expense.getAmount(),
                expense.getDate(),
                expense.getNotes()
        );
    }
}
