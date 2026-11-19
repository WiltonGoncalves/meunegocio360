package br.com.meunegocio360.core.auth.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(name = "UsuarioAutenticado")
public record AuthUserResponse(UUID id, String nome, String email, String perfil) {}
