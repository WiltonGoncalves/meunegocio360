package br.com.meunegocio360.core.auth.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "refresh_token")
public class RefreshTokenJpaEntity {

  @Id
  @JdbcTypeCode(SqlTypes.BINARY)
  @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false)
  private UUID id;

  @JdbcTypeCode(SqlTypes.BINARY)
  @Column(name = "usuario_id", columnDefinition = "BINARY(16)", nullable = false)
  private UUID usuarioId;

  @JdbcTypeCode(SqlTypes.BINARY)
  @Column(name = "familia_id", columnDefinition = "BINARY(16)", nullable = false)
  private UUID familiaId;

  @Column(name = "token_hash", nullable = false, unique = true, length = 64)
  private String tokenHash;

  @Column(name = "expira_em", nullable = false)
  private Instant expiraEm;

  @Column(name = "revogado_em")
  private Instant revogadoEm;

  @Column(name = "criado_em", nullable = false)
  private Instant criadoEm;

  protected RefreshTokenJpaEntity() {}

  public RefreshTokenJpaEntity(
      UUID id,
      UUID usuarioId,
      UUID familiaId,
      String tokenHash,
      Instant expiraEm,
      Instant revogadoEm,
      Instant criadoEm) {
    this.id = id;
    this.usuarioId = usuarioId;
    this.familiaId = familiaId;
    this.tokenHash = tokenHash;
    this.expiraEm = expiraEm;
    this.revogadoEm = revogadoEm;
    this.criadoEm = criadoEm;
  }

  public UUID getId() {
    return id;
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

  public Instant getCriadoEm() {
    return criadoEm;
  }
}
