package br.com.kauangoncalves.drywall_api.auth;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.kauangoncalves.drywall_api.common.exception.DuplicateEmailException;
import br.com.kauangoncalves.drywall_api.jwt.JwtService;
import br.com.kauangoncalves.drywall_api.user.User;
import br.com.kauangoncalves.drywall_api.user.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public User register(RegisterRequest request) {

        if (!request.password().equals(request.passwordConfirmation())) {
            throw new IllegalArgumentException("As senhas não coincidem");
        }

        String normalizedEmail = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateEmailException("O e-mail '" + normalizedEmail + "' já está cadastrado no sistema.");
        }

        String passwordHash = passwordEncoder.encode(request.password());

        User user = new User(
                request.name().trim(),
                normalizedEmail,
                request.phone() != null ? request.phone().trim() : null,
                passwordHash
        );

        return userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {

        String normalizedEmail = normalizeEmail(request.email());

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("E-mail ou senha inválidos"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("E-mail ou senha inválidos");
        }

        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail()
        );

        return new LoginResponse(token);
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("E-mail é obrigatório");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
