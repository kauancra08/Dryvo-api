package br.com.kauangoncalves.drywall_api.quote;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.kauangoncalves.drywall_api.client.Client;
import br.com.kauangoncalves.drywall_api.user.User;

class QuoteCalculationTest {

    @Test
    @DisplayName("Deve inicializar orçamentos com valores zerados e status DRAFT")
    void shouldInitializeQuoteWithZeroValuesAndDraftStatus() {
        User user = new User("Kauan", "kauan@example.com", "hash", null);
        Client client = new Client(user, "Cliente", null, null, null, null, null, null, null, null);

        Quote quote = new Quote(user, client, "Instalação de parede Drywall");

        assertEquals(QuoteStatus.DRAFT, quote.getStatus());
        assertEquals(BigDecimal.ZERO, quote.getLaborCost());
        assertEquals(BigDecimal.ZERO, quote.getOtherCost());
        assertEquals(BigDecimal.ZERO, quote.getDiscount());
        assertEquals(BigDecimal.ZERO, quote.getSubtotal());
        assertEquals(BigDecimal.ZERO, quote.getTotal());
    }

    @Test
    @DisplayName("Deve calcular corretamente o total do QuoteItem multiplicando quantidade por preço unitário")
    void shouldCalculateQuoteItemTotalCorrectly() {
        User user = new User("Kauan", "kauan@example.com", "hash", null);
        Client client = new Client(user, "Cliente", null, null, null, null, null, null, null, null);
        Quote quote = new Quote(user, client, "Obra Drywall");

        QuoteItem item = new QuoteItem(
                quote,
                null,
                "Chapa Standard 12.5mm",
                "M2",
                new BigDecimal("25.500"),
                new BigDecimal("40.00")
        );

        // 25.5 * 40.00 = 1020.00
        assertEquals(0, new BigDecimal("1020.00").compareTo(item.getTotal()));
    }
}
