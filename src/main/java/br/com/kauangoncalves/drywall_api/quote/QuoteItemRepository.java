package br.com.kauangoncalves.drywall_api.quote;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteItemRepository extends JpaRepository<QuoteItem, Long> {

    List<QuoteItem> findAllByQuoteIdOrderByIdAsc(Long quoteId);

    void deleteAllByQuoteId(Long quoteId);
}
