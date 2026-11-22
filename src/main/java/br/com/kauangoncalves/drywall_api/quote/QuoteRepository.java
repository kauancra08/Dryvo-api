package br.com.kauangoncalves.drywall_api.quote;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

    List<Quote> findAllByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    boolean existsByClientId(Long clientId);

    void deleteAllByClientId(Long clientId);
}