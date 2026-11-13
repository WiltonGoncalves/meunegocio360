package br.com.meunegocio360.core.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "refresh_token")
public class RefreshToken {

  @Id
  @UuidGenerator
  @Column(name = "id", columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "usuario_id", nullable = false, columnDefinition = "BINARY(16)")
  private UUID usuarioId;

  @Column(name = "familia_id", nullable = false, columnDefinition = "BINARY(16)")
  private UUID familiaId;

  @Column(name = "token_hash", nullable = false, unique = true, length = 64)
  private String tokenHash;

  @Column(name = "expira_em", nullable = false, columnDefinition = "DATETIME(6)")
  private Instant expiraEm;

  @Column(name = "revogado_em", columnDefinition = "DATETIME(6)")
  private Instant revogadoEm;

  @Column(name = "criado_em", nullable = false, columnDefinition = "DATETIME(6)")
  private Instant criadoEm;

  protected RefreshToken() {}

  public RefreshToken(
      UUID usuarioId, UUID familiaId, String tokenHash, Instant expiraEm, Instant agora) {
    this.usuarioId = usuarioId;
    this.familiaId = familiaId;
    this.tokenHash = tokenHash;
    this.expiraEm = expiraEm;
    this.criadoEm = agora;
  }

  public void revogar(Instant agora) {
    if (revogadoEm == null) {
      revogadoEm = agora;
    }
  }

  public boolean estaValido(Instant agora) {
    return revogadoEm == null && expiraEm.isAfter(agora);
  }

  public UUID getUsuarioId() {
    return usuarioId;
  }

  public UUID getFamiliaId() {
    return familiaId;
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public Instant getExpiraEm() {
    return expiraEm;
  }

  public Instant getRevogadoEm() {
    return revogadoEm;
  }
}
