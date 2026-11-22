package br.com.kauangoncalves.drywall_api.client;

public record ClientResponse(
        Long id,
        String name,
        String phone,
        String email,
        String document,
        String address,
        String city,
        String state,
        String zipCode,
        String notes
) {

    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getName(),
                client.getPhone(),
                client.getEmail(),
                client.getDocument(),
                client.getAddress(),
                client.getCity(),
                client.getState(),
                client.getZipCode(),
                client.getNotes()
        );
    }
}