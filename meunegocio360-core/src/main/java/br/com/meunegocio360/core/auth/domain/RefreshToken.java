package br.com.meunegocio360.core.auth.domain;

import java.time.Instant;
import java.util.UUID;

public record RefreshToken(
    UUID id,
    UUID usuarioId,
    UUID familiaId,
    String tokenHash,
    Instant expiraEm,
    Instant revogadoEm,
    Instant criadoEm) {

  public boolean expirado(Instant agora) {
    return !agora.isBefore(expiraEm);
  }

  public boolean revogado() {
    return revogadoEm != null;
  }
}
