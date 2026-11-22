package br.com.kauangoncalves.drywall_api.client;

import br.com.kauangoncalves.drywall_api.quote.Quote;
import br.com.kauangoncalves.drywall_api.quote.QuoteResponse;
import br.com.kauangoncalves.drywall_api.quote.QuoteStatus;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/clients")
public class ClientHistoryController {

    private final EntityManager entityManager;

    public ClientHistoryController(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @GetMapping("/{id}/history")
    @Transactional
    public ClientHistoryResponse history(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        Client client = entityManager.createQuery(
                        "select c from Client c where c.id = :id and c.owner.id = :ownerId",
                        Client.class)
                .setParameter("id", id)
                .setParameter("ownerId", userId)
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cliente não encontrado"
                ));

        List<Quote> quotes = entityManager.createQuery(
                        "select q from Quote q " +
                        "join fetch q.client " +
                        "where q.client.id = :clientId " +
                        "and q.owner.id = :ownerId " +
                        "order by q.createdAt desc",
                        Quote.class)
                .setParameter("clientId", id)
                .setParameter("ownerId", userId)
                .getResultList();

        BigDecimal totalQuoted = sum(quotes);

        BigDecimal totalRealized = sumByStatus(
                quotes,
                QuoteStatus.APPROVED,
                QuoteStatus.COMPLETED
        );

        BigDecimal totalPending = sumByStatus(
                quotes,
                QuoteStatus.DRAFT,
                QuoteStatus.SENT
        );

        BigDecimal totalRejected = sumByStatus(
                quotes,
                QuoteStatus.REJECTED,
                QuoteStatus.CANCELLED
        );

        return new ClientHistoryResponse(
                client.getId(),
                client.getName(),
                totalQuoted,
                totalRealized,
                totalPending,
                totalRejected,
                quotes.stream()
                        .map(QuoteResponse::from)
                        .toList()
        );
    }

    private BigDecimal sum(List<Quote> quotes) {
        return quotes.stream()
                .map(Quote::getTotal)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumByStatus(
            List<Quote> quotes,
            QuoteStatus... statuses
    ) {
        return quotes.stream()
                .filter(q -> Arrays.asList(statuses).contains(q.getStatus()))
                .map(Quote::getTotal)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}