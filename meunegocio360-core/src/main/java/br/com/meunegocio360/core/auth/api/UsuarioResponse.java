package br.com.meunegocio360.core.auth.api;

import java.util.UUID;

public record UsuarioResponse(UUID id, String nome, String email, String perfil) {}
