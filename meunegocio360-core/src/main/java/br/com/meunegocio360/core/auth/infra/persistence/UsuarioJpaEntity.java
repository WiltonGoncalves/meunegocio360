package br.com.meunegocio360.core.auth.infra.persistence;

import br.com.meunegocio360.core.auth.domain.Perfil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "usuario")
public class UsuarioJpaEntity {

  @Id
  @JdbcTypeCode(SqlTypes.BINARY)
  @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false)
  private UUID id;

  @Column(name = "nome", nullable = false, length = 120)
  private String nome;

  @Column(name = "email", nullable = false, unique = true, length = 160)
  private String email;

  @Column(name = "senha_hash", nullable = false, length = 100)
  private String senhaHash;

  @Enumerated(EnumType.STRING)
  @Column(name = "perfil", nullable = false, length = 20)
  private Perfil perfil;

  @Column(name = "ativo", nullable = false)
  private boolean ativo;

  @Column(name = "tentativas_falhas", nullable = false)
  private int tentativasFalhas;

  @Column(name = "bloqueado_ate")
  private Instant bloqueadoAte;

  @Column(name = "ultimo_login_em")
  private Instant ultimoLoginEm;

  @Column(name = "criado_em", nullable = false)
  private Instant criadoEm;

  @Column(name = "atualizado_em", nullable = false)
  private Instant atualizadoEm;

  @Version
  @Column(name = "versao", nullable = false)
  private Long versao;

  protected UsuarioJpaEntity() {}

  public UsuarioJpaEntity(
      UUID id,
      String nome,
      String email,
      String senhaHash,
      Perfil perfil,
      boolean ativo,
      int tentativasFalhas,
      Instant bloqueadoAte,
      Instant ultimoLoginEm,
      Instant criadoEm,
      Instant atualizadoEm,
      Long versao) {
    this.id = id;
    this.nome = nome;
    this.email = email;
    this.senhaHash = senhaHash;
    this.perfil = perfil;
    this.ativo = ativo;
    this.tentativasFalhas = tentativasFalhas;
    this.bloqueadoAte = bloqueadoAte;
    this.ultimoLoginEm = ultimoLoginEm;
    this.criadoEm = criadoEm;
    this.atualizadoEm = atualizadoEm;
    this.versao = versao;
  }

  public UUID getId() {
    return id;
  }

  public String getNome() {
    return nome;
  }

  public String getEmail() {
    return email;
  }

  public String getSenhaHash() {
    return senhaHash;
  }

  public Perfil getPerfil() {
    return perfil;
  }

  public boolean isAtivo() {
    return ativo;
  }

  public int getTentativasFalhas() {
    return tentativasFalhas;
  }

  public Instant getBloqueadoAte() {
    return bloqueadoAte;
  }

  public Instant getUltimoLoginEm() {
    return ultimoLoginEm;
  }

  public Instant getCriadoEm() {
    return criadoEm;
  }

  public Instant getAtualizadoEm() {
    return atualizadoEm;
  }

  public Long getVersao() {
    return versao;
  }
}
