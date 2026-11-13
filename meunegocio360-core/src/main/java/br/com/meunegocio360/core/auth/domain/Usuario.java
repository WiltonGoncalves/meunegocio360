package br.com.meunegocio360.core.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "usuario")
public class Usuario {

  @Id
  @UuidGenerator
  @Column(name = "id", columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(nullable = false, length = 120)
  private String nome;

  @Column(nullable = false, unique = true, length = 160)
  private String email;

  @Column(name = "senha_hash", nullable = false, length = 100)
  private String senhaHash;

  @Column(nullable = false, length = 20)
  private String perfil;

  @Column(nullable = false)
  private boolean ativo = true;

  @Column(name = "tentativas_falhas", nullable = false)
  private int tentativasFalhas;

  @Column(name = "bloqueado_ate", columnDefinition = "DATETIME(6)")
  private Instant bloqueadoAte;

  @Column(name = "ultimo_login_em", columnDefinition = "DATETIME(6)")
  private Instant ultimoLoginEm;

  @Column(name = "criado_em", nullable = false, columnDefinition = "DATETIME(6)")
  private Instant criadoEm;

  @Column(name = "atualizado_em", nullable = false, columnDefinition = "DATETIME(6)")
  private Instant atualizadoEm;

  @Version
  @Column(nullable = false)
  private long versao;

  protected Usuario() {}

  public Usuario(String nome, String email, String senhaHash, String perfil, Instant agora) {
    this.nome = nome;
    this.email = email.trim().toLowerCase(Locale.ROOT);
    this.senhaHash = senhaHash;
    this.perfil = perfil;
    this.criadoEm = agora;
    this.atualizadoEm = agora;
  }

  public void registrarFalha(Instant agora, int maxTentativas, int bloqueioMinutos) {
    tentativasFalhas++;
    atualizadoEm = agora;
    if (tentativasFalhas >= maxTentativas) {
      bloqueadoAte = agora.plusSeconds(bloqueioMinutos * 60L);
    }
  }

  public void registrarLoginBemSucedido(Instant agora) {
    tentativasFalhas = 0;
    bloqueadoAte = null;
    ultimoLoginEm = agora;
    atualizadoEm = agora;
  }

  public void limparBloqueioExpirado(Instant agora) {
    if (bloqueadoAte != null && !bloqueadoAte.isAfter(agora)) {
      bloqueadoAte = null;
      tentativasFalhas = 0;
      atualizadoEm = agora;
    }
  }

  public boolean estaBloqueado(Instant agora) {
    return bloqueadoAte != null && bloqueadoAte.isAfter(agora);
  }

  public boolean isAtivo() {
    return ativo;
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

  public String getPerfil() {
    return perfil;
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

  public void desativar(Instant agora) {
    ativo = false;
    atualizadoEm = agora;
  }
}
