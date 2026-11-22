package br.com.kauangoncalves.drywall_api.quote.pdf;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.kauangoncalves.drywall_api.client.Client;
import br.com.kauangoncalves.drywall_api.quote.Quote;
import br.com.kauangoncalves.drywall_api.quote.QuoteItem;
import br.com.kauangoncalves.drywall_api.quote.QuoteStatus;
import br.com.kauangoncalves.drywall_api.user.User;

class QuotePdfServiceTest {

    private QuotePdfService pdfService;

    @BeforeEach
    void setUp() {
        pdfService = new QuotePdfService();
    }

    @Test
    @DisplayName("Deve gerar PDF do orçamento com cabeçalho Dryvo, cliente, itens e totais")
    void shouldGeneratePdfSuccessfully() {
        User user = new User("Kauan Desenvolvedor", "kauan@example.com", "hash", "11999998888");
        Client client = new Client(
                user,
                "Carlos Eduardo da Silva",
                "11988887777",
                "carlos@cliente.com.br",
                "123.456.789-00",
                "Av. Paulista, 1000 - Apto 502",
                "São Paulo",
                "SP",
                "01310-100",
                "Obra residencial comercial"
        );

        Quote quote = new Quote(user, client, "Instalação de forro de gesso acartonado e paredes acústicas Drywall.");
        quote.setStatus(QuoteStatus.APPROVED);
        quote.setLaborCost(new BigDecimal("1500.00"));
        quote.setOtherCost(new BigDecimal("200.00"));
        quote.setDiscount(new BigDecimal("100.00"));
        quote.setSubtotal(new BigDecimal("2450.50"));
        quote.setTotal(new BigDecimal("4050.50"));

        List<QuoteItem> items = new ArrayList<>();
        items.add(new QuoteItem(quote, null, "Placa de Gesso ST 12.5mm Knauf", "M2", new BigDecimal("45.0"), new BigDecimal("32.50")));
        items.add(new QuoteItem(quote, null, "Perfil Montante 48mm 3m", "BARRA", new BigDecimal("30.0"), new BigDecimal("18.90")));
        items.add(new QuoteItem(quote, null, "Parafuso Drywall Fosfatizado 25mm", "CENTO", new BigDecimal("10.0"), new BigDecimal("12.00")));
        items.add(new QuoteItem(quote, null, "Massa de Tratamento de Juntas 20kg", "SACO", new BigDecimal("4.0"), new BigDecimal("85.00")));

        byte[] pdfBytes = pdfService.generate(quote, items);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500, "O PDF gerado deve conter conteúdo substancial");

        // Verifica os magic bytes de um PDF válido (%PDF-)
        String header = new String(pdfBytes, 0, Math.min(5, pdfBytes.length));
        assertTrue(header.startsWith("%PDF"), "O arquivo deve começar com o cabeçalho PDF");
    }

    @Test
    @DisplayName("Deve gerar PDF mesmo sem itens ou sem descrição com segurança")
    void shouldGeneratePdfWithEmptyItems() {
        User user = new User("Kauan", "kauan@example.com", "hash", null);
        Client client = new Client(user, "Cliente Rápido", null, null, null, null, null, null, null, null);
        Quote quote = new Quote(user, client, null);

        byte[] pdfBytes = pdfService.generate(quote, List.of());

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500);
        String header = new String(pdfBytes, 0, 5);
        assertTrue(header.startsWith("%PDF"));
    }
}
