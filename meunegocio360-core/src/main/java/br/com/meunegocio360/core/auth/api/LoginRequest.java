package br.com.meunegocio360.core.auth.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @Schema(example = "admin@meunegocio360.com.br")
        @NotBlank(message = "é obrigatório")
        @Email(message = "deve ser um e-mail válido")
        @Size(max = 160)
        String email,
    @Schema(example = "********")
        @NotBlank(message = "é obrigatória")
        @Size(min = 8, max = 72, message = "deve conter de 8 a 72 caracteres")
        String senha) {}
