package br.com.kauangoncalves.drywall_api.quote;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quotes")
public class QuoteController {
    private final QuoteService quoteService;
    public QuoteController(QuoteService quoteService) { this.quoteService = quoteService; }

    @PostMapping
    public Quote create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody QuoteRequest request) {
        return quoteService.create(Long.valueOf(jwt.getSubject()), request);
    }

    @GetMapping
    public List<QuoteResponse> findAll(@AuthenticationPrincipal Jwt jwt) {
        return quoteService.findAll(Long.valueOf(jwt.getSubject()));
    }

    @GetMapping("/{id}")
    public QuoteDetailsResponse findOne(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return quoteService.findDetails(Long.valueOf(jwt.getSubject()), id);
    }

    @PutMapping("/{id}")
    public Quote update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @Valid @RequestBody QuoteRequest request) {
        return quoteService.update(Long.valueOf(jwt.getSubject()), id, request);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generatePdf(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        byte[] pdf = quoteService.generatePdf(Long.valueOf(jwt.getSubject()), id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline().filename("orcamento-" + id + ".pdf").build());
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
