package br.com.kauangoncalves.drywall_api.user;

public record UserResponse(
        Long id,
        String name,
        String email,
        String phone
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone()
        );
    }
}
