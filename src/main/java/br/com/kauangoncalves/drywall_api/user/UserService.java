package br.com.kauangoncalves.drywall_api.user;

import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.kauangoncalves.drywall_api.common.exception.DuplicateEmailException;
import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);

        // Se o usuário está alterando o e-mail, verifica se outro usuário já o utiliza
        if (!normalizedEmail.equalsIgnoreCase(user.getEmail())) {
            userRepository.findByEmailIgnoreCase(normalizedEmail).ifPresent(otherUser -> {
                if (!otherUser.getId().equals(userId)) {
                    throw new DuplicateEmailException("O e-mail '" + normalizedEmail + "' já está em uso por outro usuário.");
                }
            });
        }

        user.setName(request.name().trim());
        user.setEmail(normalizedEmail);
        user.setPhone(request.phone() != null ? request.phone().trim() : null);

        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }
}
