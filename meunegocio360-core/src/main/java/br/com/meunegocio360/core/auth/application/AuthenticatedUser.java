package br.com.meunegocio360.core.auth.application;

import br.com.meunegocio360.core.auth.domain.Perfil;
import java.util.UUID;

public record AuthenticatedUser(UUID id, String nome, String email, Perfil perfil) {}
