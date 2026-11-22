package br.com.kauangoncalves.drywall_api.finance;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/finance")
public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping("/summary")
    public FinanceSummaryResponse summary(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        YearMonth current = YearMonth.now();
        int resolvedYear = year != null ? year : current.getYear();
        int resolvedMonth = month != null ? month : current.getMonthValue();

        validateMonth(resolvedMonth);

        return financeService.summary(
                userId(jwt),
                resolvedYear,
                resolvedMonth
        );
    }

    @GetMapping("/history")
    public List<FinanceHistoryResponse> history(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "6") int months
    ) {
        return financeService.history(userId(jwt), months);
    }

    @GetMapping("/expenses")
    public List<FinanceExpenseResponse> findExpenses(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        YearMonth current = YearMonth.now();
        int resolvedYear = year != null ? year : current.getYear();
        int resolvedMonth = month != null ? month : current.getMonthValue();

        validateMonth(resolvedMonth);

        return financeService.findExpenses(
                userId(jwt),
                resolvedYear,
                resolvedMonth
        );
    }

    @PostMapping("/expenses")
    @ResponseStatus(CREATED)
    public FinanceExpenseResponse createExpense(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody FinanceExpenseRequest request
    ) {
        return financeService.create(userId(jwt), request);
    }

    @PutMapping("/expenses/{id}")
    public FinanceExpenseResponse updateExpense(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody FinanceExpenseRequest request
    ) {
        return financeService.update(userId(jwt), id, request);
    }

    @DeleteMapping("/expenses/{id}")
    @ResponseStatus(NO_CONTENT)
    public void deleteExpense(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        financeService.delete(userId(jwt), id);
    }

    private Long userId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

    private void validateMonth(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Mês inválido");
        }
    }
}
