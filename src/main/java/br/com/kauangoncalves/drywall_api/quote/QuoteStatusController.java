package br.com.kauangoncalves.drywall_api.quote;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Set;

@RestController
@RequestMapping("/quotes")
public class QuoteStatusController {

    private static final Set<String> ALLOWED = Set.of(
            "DRAFT",
            "SENT",
            "APPROVED",
            "REJECTED",
            "CANCELLED",
            "COMPLETED"
    );

    private final EntityManager entityManager;

    public QuoteStatusController(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @PutMapping("/{id}/status")
    @Transactional
    public QuoteResponse updateStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody QuoteStatusRequest request
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        String status = request.status()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (!ALLOWED.contains(status)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status inválido. Use DRAFT, SENT, APPROVED, REJECTED ou CANCELLED ou COMPLETED."
            );
        }

        Quote quote = findOwnedQuote(id, userId);

        entityManager.createNativeQuery(
                        "update quotes " +
                        "set status = :status, updated_at = CURRENT_TIMESTAMP " +
                        "where id = :id")
                .setParameter("status", status)
                .setParameter("id", id)
                .executeUpdate();

        entityManager.clear();

        Quote updated = findOwnedQuote(id, userId);

        return QuoteResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        Quote quote = findOwnedQuote(id, userId);

        entityManager.remove(quote);
        entityManager.flush();
    }

    private Quote findOwnedQuote(Long id, Long userId) {
        try {
            return entityManager.createQuery(
                            "select q from Quote q " +
                            "join fetch q.client " +
                            "where q.id = :id " +
                            "and q.owner.id = :ownerId",
                            Quote.class)
                    .setParameter("id", id)
                    .setParameter("ownerId", userId)
                    .getSingleResult();

        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Orçamento não encontrado"
            );
        }
    }
}