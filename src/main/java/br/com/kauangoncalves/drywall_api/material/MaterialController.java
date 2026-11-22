package br.com.kauangoncalves.drywall_api.material;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/materials")
public class MaterialController {
    private final MaterialService materialService;
    public MaterialController(MaterialService materialService) { this.materialService = materialService; }
    @PostMapping
    public Material create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody MaterialRequest request) {
        return materialService.create(Long.valueOf(jwt.getSubject()), request);
    }
    @GetMapping
    public List<Material> findAll(@AuthenticationPrincipal Jwt jwt) {
        return materialService.findAll(Long.valueOf(jwt.getSubject()));
    }
    @PutMapping("/{id}")
    public Material update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @Valid @RequestBody MaterialRequest request) {
        return materialService.update(Long.valueOf(jwt.getSubject()), id, request);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        materialService.delete(Long.valueOf(jwt.getSubject()), id);
        return ResponseEntity.noContent().build();
    }
}
