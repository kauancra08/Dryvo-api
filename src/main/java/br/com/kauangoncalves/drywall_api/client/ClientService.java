package br.com.kauangoncalves.drywall_api.client;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.kauangoncalves.drywall_api.quote.QuoteRepository;
import br.com.kauangoncalves.drywall_api.user.User;
import br.com.kauangoncalves.drywall_api.user.UserRepository;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final QuoteRepository quoteRepository;

    public ClientService(
            ClientRepository clientRepository,
            UserRepository userRepository,
            QuoteRepository quoteRepository) {

        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.quoteRepository = quoteRepository;
    }

    @Transactional
    public ClientResponse create(Long userId, ClientRequest request) {

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Client client = new Client(
                owner,
                request.name(),
                request.phone(),
                request.email(),
                request.document(),
                request.address(),
                request.city(),
                request.state(),
                request.zipCode(),
                request.notes()
        );

        return ClientResponse.from(clientRepository.save(client));
    }

    @Transactional
    public ClientResponse update(
            Long userId,
            Long clientId,
            ClientRequest request) {

        Client client = clientRepository
                .findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

        if (!client.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Cliente não pertence ao usuário");
        }

        client.setName(request.name());
        client.setPhone(request.phone());
        client.setEmail(request.email());
        client.setDocument(request.document());
        client.setAddress(request.address());
        client.setCity(request.city());
        client.setState(request.state());
        client.setZipCode(request.zipCode());
        client.setNotes(request.notes());

        return ClientResponse.from(clientRepository.save(client));
    }

    @Transactional
    public void delete(Long userId, Long clientId) {

        Client client = clientRepository
                .findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

        if (!client.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Cliente não pertence ao usuário");
        }

        quoteRepository.deleteAllByClientId(clientId);

        clientRepository.delete(client);
    }
}
