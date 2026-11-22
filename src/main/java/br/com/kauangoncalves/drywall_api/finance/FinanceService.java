package br.com.kauangoncalves.drywall_api.finance;

import br.com.kauangoncalves.drywall_api.quote.Quote;
import br.com.kauangoncalves.drywall_api.quote.QuoteRepository;
import br.com.kauangoncalves.drywall_api.user.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class FinanceService {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Sao_Paulo");
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final FinanceExpenseRepository expenseRepository;
    private final QuoteRepository quoteRepository;
    private final EntityManager entityManager;

    public FinanceService(
            FinanceExpenseRepository expenseRepository,
            QuoteRepository quoteRepository,
            EntityManager entityManager
    ) {
        this.expenseRepository = expenseRepository;
        this.quoteRepository = quoteRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public FinanceExpenseResponse create(Long userId, FinanceExpenseRequest request) {
        FinanceExpense expense = new FinanceExpense();
        expense.setOwner(getUserReference(userId));
        apply(expense, request);
        return FinanceExpenseResponse.from(expenseRepository.save(expense));
    }

    @Transactional
    public FinanceExpenseResponse update(Long userId, Long id, FinanceExpenseRequest request) {
        FinanceExpense expense = findOwnedExpense(userId, id);
        apply(expense, request);
        return FinanceExpenseResponse.from(expenseRepository.save(expense));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        FinanceExpense expense = findOwnedExpense(userId, id);
        expenseRepository.delete(expense);
    }

    @Transactional
    public List<FinanceExpenseResponse> findExpenses(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        return expenseRepository
                .findAllByOwnerIdAndDateBetweenOrderByDateDescIdDesc(userId, start, end)
                .stream()
                .map(FinanceExpenseResponse::from)
                .toList();
    }

    @Transactional
    public FinanceSummaryResponse summary(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        List<Quote> quotes = quoteRepository.findAllByOwnerIdOrderByCreatedAtDesc(userId);
        List<FinanceExpense> expenses = expensesForMonth(userId, yearMonth);

        FinancialTotals totals = calculateForMonth(quotes, expenses, yearMonth);

        return new FinanceSummaryResponse(
                year,
                month,
                totals.grossRevenue,
                totals.materialCosts,
                totals.laborCosts,
                totals.quoteOtherCosts,
                totals.totalCosts,
                totals.grossProfit,
                totals.expenses,
                totals.netProfit,
                totals.quoteCount
        );
    }

    @Transactional
    public List<FinanceHistoryResponse> history(Long userId, int months) {
        int safeMonths = Math.max(1, Math.min(months, 24));
        YearMonth current = YearMonth.now(BUSINESS_ZONE);
        List<Quote> quotes = quoteRepository.findAllByOwnerIdOrderByCreatedAtDesc(userId);
        List<FinanceExpense> expenses = expenseRepository.findAllByOwnerIdOrderByDateDescIdDesc(userId);

        List<FinanceHistoryResponse> result = new ArrayList<>();

        for (int i = safeMonths - 1; i >= 0; i--) {
            YearMonth target = current.minusMonths(i);
            FinancialTotals totals = calculateForMonth(quotes, expenses, target);

            result.add(new FinanceHistoryResponse(
                    target.getYear(),
                    target.getMonthValue(),
                    totals.grossRevenue,
                    totals.totalCosts,
                    totals.grossProfit,
                    totals.expenses,
                    totals.netProfit,
                    totals.quoteCount
            ));
        }

        return result;
    }

    private FinancialTotals calculateForMonth(
            List<Quote> quotes,
            List<FinanceExpense> expenses,
            YearMonth target
    ) {
        BigDecimal grossRevenue = ZERO;
        BigDecimal materialCosts = ZERO;
        BigDecimal laborCosts = ZERO;
        BigDecimal quoteOtherCosts = ZERO;
        BigDecimal laborDiscounts = ZERO;
        long quoteCount = 0;

        for (Quote quote : quotes) {
            if (!isRealizedQuote(quote)) {
                continue;
            }

            if (!sameMonth(quote.getCreatedAt(), target)) {
                continue;
            }

            grossRevenue = add(grossRevenue, quote.getTotal());
            materialCosts = add(materialCosts, quote.getSubtotal());
            laborCosts = add(laborCosts, quote.getLaborCost());
            quoteOtherCosts = add(quoteOtherCosts, quote.getOtherCost());
            laborDiscounts = add(laborDiscounts, quote.getDiscount());
            quoteCount++;
        }

        // O subtotal dos materiais representa o valor cobrado no orçamento,
        // pois o preço unitário usado no orçamento é o preço de venda.
        // Portanto, não devemos subtrair esse subtotal novamente como se fosse
        // um custo interno; isso zerava ou distorcia o lucro.
        // A mão de obra representa o que o prestador ganha pelo serviço.
        // O desconto reduz somente a mão de obra.
        //
        // Materiais não são lucro: o valor deles apenas passa pelo orçamento.
        // Outros custos (ex.: frete) também não reduzem o lucro, pois são
        // valores que o cliente paga e que não saem do caixa do prestador.
        //
        // Portanto, neste modelo, o lucro bruto é a mão de obra líquida.
        BigDecimal laborProfit = laborCosts
                .subtract(laborDiscounts)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalCosts = ZERO;

        BigDecimal grossProfit = laborProfit;

        BigDecimal totalExpenses = ZERO;
        for (FinanceExpense expense : expenses) {
            if (YearMonth.from(expense.getDate()).equals(target)) {
                totalExpenses = add(totalExpenses, expense.getAmount());
            }
        }

        BigDecimal netProfit = grossProfit
                .subtract(totalExpenses)
                .setScale(2, RoundingMode.HALF_UP);

        return new FinancialTotals(
                grossRevenue,
                materialCosts,
                laborCosts,
                quoteOtherCosts,
                totalCosts,
                grossProfit,
                totalExpenses,
                netProfit,
                quoteCount
        );
    }

    private List<FinanceExpense> expensesForMonth(Long userId, YearMonth yearMonth) {
        return expenseRepository.findAllByOwnerIdAndDateBetweenOrderByDateDescIdDesc(
                userId,
                yearMonth.atDay(1),
                yearMonth.atEndOfMonth()
        );
    }

    private boolean isRealizedQuote(Quote quote) {
        if (quote.getStatus() == null) {
            return false;
        }

        String status = quote.getStatus().name();
        return "APPROVED".equals(status) || "COMPLETED".equals(status);
    }

    private boolean sameMonth(Instant instant, YearMonth target) {
        if (instant == null) {
            return false;
        }

        return YearMonth.from(instant.atZone(BUSINESS_ZONE)).equals(target);
    }

    private BigDecimal add(BigDecimal current, BigDecimal value) {
        if (value == null) {
            return current;
        }

        return current.add(value).setScale(2, RoundingMode.HALF_UP);
    }

    private void apply(FinanceExpense expense, FinanceExpenseRequest request) {
        expense.setDescription(request.description().trim());
        expense.setCategory(normalize(request.category()));
        expense.setAmount(request.amount().setScale(2, RoundingMode.HALF_UP));
        expense.setDate(request.date());
        expense.setNotes(normalize(request.notes()));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private FinanceExpense findOwnedExpense(Long userId, Long id) {
        FinanceExpense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Despesa não encontrada"));

        if (!expense.getOwner().getId().equals(userId)) {
            throw new EntityNotFoundException("Despesa não encontrada");
        }

        return expense;
    }

    private User getUserReference(Long userId) {
        return entityManager.getReference(User.class, userId);
    }

    private record FinancialTotals(
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
}
