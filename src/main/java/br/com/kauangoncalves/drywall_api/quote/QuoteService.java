package br.com.kauangoncalves.drywall_api.quote;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.kauangoncalves.drywall_api.client.Client;
import br.com.kauangoncalves.drywall_api.client.ClientRepository;
import br.com.kauangoncalves.drywall_api.material.Material;
import br.com.kauangoncalves.drywall_api.material.MaterialRepository;
import br.com.kauangoncalves.drywall_api.quote.pdf.QuotePdfService;
import br.com.kauangoncalves.drywall_api.user.User;
import br.com.kauangoncalves.drywall_api.user.UserRepository;

@Service
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final QuoteItemRepository quoteItemRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final MaterialRepository materialRepository;
    private final QuotePdfService quotePdfService;

    public QuoteService(
            QuoteRepository quoteRepository,
            QuoteItemRepository quoteItemRepository,
            UserRepository userRepository,
            ClientRepository clientRepository,
            MaterialRepository materialRepository,
            QuotePdfService quotePdfService) {
        this.quoteRepository = quoteRepository;
        this.quoteItemRepository = quoteItemRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.materialRepository = materialRepository;
        this.quotePdfService = quotePdfService;
    }

    @Transactional
    public Quote create(Long userId, QuoteRequest request) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        Client client = findOwnedClient(userId, request.clientId());
        Quote quote = new Quote(owner, client, request.description());
        applyFinancialFields(quote, request);
        quoteRepository.save(quote);
        replaceItems(userId, quote, request.items());
        recalculate(quote);
        return quoteRepository.save(quote);
    }

    @Transactional
    public Quote update(Long userId, Long quoteId, QuoteRequest request) {
        Quote quote = findOwnedQuote(userId, quoteId);
        Client client = findOwnedClient(userId, request.clientId());
        quote.setDescription(request.description());
        quote.setClient(client);
        applyFinancialFields(quote, request);
        quoteItemRepository.deleteAllByQuoteId(quoteId);
        quoteItemRepository.flush();
        replaceItems(userId, quote, request.items());
        recalculate(quote);
        return quoteRepository.save(quote);
    }

    @Transactional(readOnly = true)
    public QuoteDetailsResponse findDetails(Long userId, Long quoteId) {
        Quote quote = findOwnedQuote(userId, quoteId);
        List<QuoteItemResponse> items = quoteItemRepository
                .findAllByQuoteIdOrderByIdAsc(quoteId)
                .stream()
                .map(QuoteItemResponse::from)
                .toList();
        return new QuoteDetailsResponse(QuoteResponse.from(quote), items);
    }

    @Transactional(readOnly = true)
    public List<QuoteResponse> findAll(Long userId) {
        return quoteRepository.findAllByOwnerIdOrderByCreatedAtDesc(userId)
                .stream().map(QuoteResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public byte[] generatePdf(Long userId, Long quoteId) {
        Quote quote = findOwnedQuote(userId, quoteId);
        List<QuoteItem> items = quoteItemRepository.findAllByQuoteIdOrderByIdAsc(quoteId);
        return quotePdfService.generate(quote, items);
    }

    private void applyFinancialFields(Quote quote, QuoteRequest request) {
        quote.setLaborCost(valueOrZero(request.laborCost()));
        quote.setOtherCost(valueOrZero(request.otherCost()));
        quote.setDiscount(valueOrZero(request.discount()));
    }

    private void replaceItems(Long userId, Quote quote, List<QuoteItemRequest> items) {
        List<QuoteItemRequest> safeItems = items == null ? List.of() : items;
        for (QuoteItemRequest itemRequest : safeItems) {
            Material material = null;
            if (itemRequest.materialId() != null) {
                material = materialRepository.findById(itemRequest.materialId())
                        .orElseThrow(() -> new IllegalArgumentException("Material não encontrado"));
                if (!material.getOwner().getId().equals(userId)) {
                    throw new IllegalArgumentException("Material não pertence ao usuário");
                }
            }
            quoteItemRepository.save(new QuoteItem(
                    quote, material, itemRequest.description(), itemRequest.unit(),
                    itemRequest.quantity(), itemRequest.unitPrice()));
        }
    }

    private void recalculate(Quote quote) {
        BigDecimal subtotal = quoteItemRepository.findAllByQuoteIdOrderByIdAsc(quote.getId())
                .stream().map(QuoteItem::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = subtotal.add(valueOrZero(quote.getLaborCost()))
                .add(valueOrZero(quote.getOtherCost()))
                .subtract(valueOrZero(quote.getDiscount()));
        if (total.compareTo(BigDecimal.ZERO) < 0) total = BigDecimal.ZERO;
        quote.setSubtotal(subtotal);
        quote.setTotal(total);
    }

    private Client findOwnedClient(Long userId, Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));
        if (!client.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Cliente não pertence ao usuário");
        }
        return client;
    }

    private Quote findOwnedQuote(Long userId, Long quoteId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Orçamento não encontrado"));
        if (!quote.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Orçamento não pertence ao usuário");
        }
        return quote;
    }

    private BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
