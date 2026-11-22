package br.com.kauangoncalves.drywall_api.auth;

import br.com.kauangoncalves.drywall_api.user.User;

public record RegisterResponse(
        Long id,
        String name,
        String email,
        String phone
) {

    public static RegisterResponse from(User user) {
        return new RegisterResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone()
        );
    }
}