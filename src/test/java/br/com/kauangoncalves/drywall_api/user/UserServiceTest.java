package br.com.kauangoncalves.drywall_api.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.kauangoncalves.drywall_api.common.exception.DuplicateEmailException;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    @Test
    @DisplayName("Deve permitir ao usuário manter o próprio e-mail ao atualizar perfil")
    void shouldAllowUserToKeepOwnEmailWhenUpdatingProfile() {
        Long userId = 1L;
        User user = new User("Kauan Original", "kauan@dryvo.com", "11999998888", "hash");
        ReflectionTestUtils.setField(user, "id", userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProfileRequest request = new UpdateProfileRequest(
                "Kauan Atualizado",
                "KAUAN@dryvo.com", // Mesmo e-mail com case diferente
                "11988887777"
        );

        UserResponse response = userService.updateProfile(userId, request);

        assertNotNull(response);
        assertEquals("Kauan Atualizado", response.name());
        assertEquals("kauan@dryvo.com", response.email());
        assertEquals("11988887777", response.phone());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Deve recusar alteração de e-mail quando este já pertence a outro usuário")
    void shouldRejectEmailUpdateWhenTakenByAnotherUser() {
        Long currentUserId = 1L;
        Long otherUserId = 2L;

        User currentUser = new User("Kauan", "kauan@dryvo.com", null, "hash");
        ReflectionTestUtils.setField(currentUser, "id", currentUserId);

        User otherUser = new User("Outro", "outro@dryvo.com", null, "hash");
        ReflectionTestUtils.setField(otherUser, "id", otherUserId);

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByEmailIgnoreCase("outro@dryvo.com")).thenReturn(Optional.of(otherUser));

        UpdateProfileRequest request = new UpdateProfileRequest(
                "Kauan",
                "outro@dryvo.com",
                null
        );

        assertThrows(DuplicateEmailException.class, () -> userService.updateProfile(currentUserId, request));
    }
}
