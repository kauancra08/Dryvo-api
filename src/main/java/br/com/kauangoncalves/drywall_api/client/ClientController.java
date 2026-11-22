package br.com.kauangoncalves.drywall_api.client;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;
    private final ClientRepository clientRepository;

    public ClientController(
            ClientService clientService,
            ClientRepository clientRepository) {

        this.clientService = clientService;
        this.clientRepository = clientRepository;
    }

    @PostMapping
    public ClientResponse create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ClientRequest request) {

        Long userId = Long.valueOf(jwt.getSubject());

        return clientService.create(userId, request);
    }

    @GetMapping
    public List<ClientResponse> findAll(
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = Long.valueOf(jwt.getSubject());

        return clientRepository
                .findAllByOwnerIdOrderByNameAsc(userId)
                .stream()
                .map(ClientResponse::from)
                .toList();
    }

    @PutMapping("/{id}")
    public ClientResponse update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody ClientRequest request) {

        Long userId = Long.valueOf(jwt.getSubject());

        return clientService.update(userId, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {

        Long userId = Long.valueOf(jwt.getSubject());

        try {
            clientService.delete(userId, id);
            return ResponseEntity.noContent().build();

        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
    }
}