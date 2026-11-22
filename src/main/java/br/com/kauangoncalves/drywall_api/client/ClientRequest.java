package br.com.kauangoncalves.drywall_api.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClientRequest(

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
        String name,

        @Size(max = 30, message = "Telefone deve ter no máximo 30 caracteres")
        String phone,

        @Email(message = "E-mail inválido")
        @Size(max = 255, message = "E-mail deve ter no máximo 255 caracteres")
        String email,

        @Size(max = 18, message = "Documento deve ter no máximo 18 caracteres")
        String document,

        @Size(max = 255, message = "Endereço deve ter no máximo 255 caracteres")
        String address,

        @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres")
        String city,

        @Size(max = 2, message = "Estado deve ter 2 caracteres")
        String state,

        @Size(max = 9, message = "CEP deve ter no máximo 9 caracteres")
        String zipCode,

        @Size(max = 2000, message = "Observações devem ter no máximo 2000 caracteres")
        String notes
) {
}