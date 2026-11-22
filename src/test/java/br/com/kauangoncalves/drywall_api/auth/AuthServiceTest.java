package br.com.kauangoncalves.drywall_api.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.kauangoncalves.drywall_api.common.exception.DuplicateEmailException;
import br.com.kauangoncalves.drywall_api.jwt.JwtService;
import br.com.kauangoncalves.drywall_api.user.User;
import br.com.kauangoncalves.drywall_api.user.UserRepository;

class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    @DisplayName("Deve permitir cadastro com e-mail novo e normalizado em minúsculas")
    void shouldRegisterNewUserSuccessfully() {
        RegisterRequest request = new RegisterRequest(
                "Novo Usuário",
                "  Usuario.Novo@Exemplo.COM  ",
                "11999998888",
                "senhaForte123",
                "senhaForte123"
        );

        when(userRepository.existsByEmailIgnoreCase("usuario.novo@exemplo.com")).thenReturn(false);
        when(passwordEncoder.encode("senhaForte123")).thenReturn("hashSeguro");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registered = authService.register(request);

        assertNotNull(registered);
        assertEquals("usuario.novo@exemplo.com", registered.getEmail());
        assertEquals("Novo Usuário", registered.getName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Deve recusar cadastro quando o e-mail já existe (mesma caixa)")
    void shouldRejectRegisterWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest(
                "Outro Usuário",
                "existente@exemplo.com",
                null,
                "senhaForte123",
                "senhaForte123"
        );

        when(userRepository.existsByEmailIgnoreCase("existente@exemplo.com")).thenReturn(true);

        DuplicateEmailException exception = assertThrows(
                DuplicateEmailException.class,
                () -> authService.register(request)
        );

        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("Deve recusar cadastro com e-mail duplicado independente de maiúsculas/minúsculas e espaços")
    void shouldRejectRegisterWhenEmailMatchesDifferentCasing() {
        RegisterRequest request = new RegisterRequest(
                "Usuário Teste",
                "  EXISTENTE@EXEMPLO.COM  ",
                null,
                "senhaForte123",
                "senhaForte123"
        );

        when(userRepository.existsByEmailIgnoreCase("existente@exemplo.com")).thenReturn(true);

        assertThrows(
                DuplicateEmailException.class,
                () -> authService.register(request)
        );
    }

    @Test
    @DisplayName("Deve recusar cadastro quando senhas não coincidem")
    void shouldRejectWhenPasswordsDoNotMatch() {
        RegisterRequest request = new RegisterRequest(
                "Usuário",
                "novo@exemplo.com",
                null,
                "senhaForte123",
                "senhaDiferente456"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );

        assertEquals("As senhas não coincidem", exception.getMessage());
    }
}
