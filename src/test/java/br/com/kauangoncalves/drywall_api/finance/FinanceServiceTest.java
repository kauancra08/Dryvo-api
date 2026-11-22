package br.com.kauangoncalves.drywall_api.finance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.kauangoncalves.drywall_api.client.Client;
import br.com.kauangoncalves.drywall_api.quote.Quote;
import br.com.kauangoncalves.drywall_api.quote.QuoteRepository;
import br.com.kauangoncalves.drywall_api.quote.QuoteStatus;
import br.com.kauangoncalves.drywall_api.user.User;
import jakarta.persistence.EntityManager;

class FinanceServiceTest {

    private FinanceExpenseRepository expenseRepository;
    private QuoteRepository quoteRepository;
    private EntityManager entityManager;
    private FinanceService financeService;

    @BeforeEach
    void setUp() {
        expenseRepository = mock(FinanceExpenseRepository.class);
        quoteRepository = mock(QuoteRepository.class);
        entityManager = mock(EntityManager.class);
        financeService = new FinanceService(expenseRepository, quoteRepository, entityManager);
    }

    @Test
    @DisplayName("Deve calcular resumo financeiro considerando apenas orçamentos realizados e deduzindo despesas")
    void shouldCalculateMonthlySummaryCorrectly() {
        Long userId = 1L;
        int year = 2026;
        int month = 10;
        YearMonth targetMonth = YearMonth.of(year, month);
        Instant quoteInstant = targetMonth.atDay(15).atStartOfDay(ZoneId.of("America/Sao_Paulo")).toInstant();

        User user = new User("Kauan", "kauan@example.com", "hash", null);
        Client client = new Client(user, "Cliente Teste", null, null, null, null, null, null, null, null);

        // Orçamento 1: Aprovado (realizado)
        Quote quote1 = new Quote(user, client, "Orçamento 1");
        quote1.setStatus(QuoteStatus.APPROVED);
        ReflectionTestUtils.setField(quote1, "createdAt", quoteInstant);
        quote1.setSubtotal(new BigDecimal("2000.00")); // Materiais
        quote1.setLaborCost(new BigDecimal("1500.00")); // Mão de obra
        quote1.setOtherCost(new BigDecimal("100.00"));  // Outros
        quote1.setDiscount(new BigDecimal("200.00"));   // Desconto
        quote1.setTotal(new BigDecimal("3400.00"));

        // Orçamento 2: Rascunho (não realizado - não deve entrar no realizado)
        Quote quote2 = new Quote(user, client, "Orçamento Rascunho");
        quote2.setStatus(QuoteStatus.DRAFT);
        ReflectionTestUtils.setField(quote2, "createdAt", quoteInstant);
        quote2.setSubtotal(new BigDecimal("5000.00"));
        quote2.setTotal(new BigDecimal("7000.00"));

        when(quoteRepository.findAllByOwnerIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(quote1, quote2));

        // Despesas operacionais do mês
        FinanceExpense expense1 = new FinanceExpense();
        expense1.setDescription("Combustível e Transporte");
        expense1.setAmount(new BigDecimal("300.00"));
        expense1.setDate(LocalDate.of(2026, 10, 10));

        when(expenseRepository.findAllByOwnerIdAndDateBetweenOrderByDateDescIdDesc(
                userId, targetMonth.atDay(1), targetMonth.atEndOfMonth()))
                .thenReturn(List.of(expense1));

        FinanceSummaryResponse summary = financeService.summary(userId, year, month);

        assertNotNull(summary);
        assertEquals(1, summary.quoteCount(), "Apenas orçamentos aprovados/concluídos devem contar");
        assertEquals(new BigDecimal("3400.00"), summary.grossRevenue());
        assertEquals(new BigDecimal("2000.00"), summary.materialCosts());
        assertEquals(new BigDecimal("1500.00"), summary.laborCosts());

        // Lucro bruto = Mão de obra líquida = 1500 - 200 = 1300.00
        assertEquals(new BigDecimal("1300.00"), summary.grossProfit());

        // Despesas = 300.00
        assertEquals(new BigDecimal("300.00"), summary.expenses());

        // Lucro líquido = Lucro bruto - Despesas = 1300.00 - 300.00 = 1000.00
        assertEquals(new BigDecimal("1000.00"), summary.netProfit());
    }
}
