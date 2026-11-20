package br.com.meunegocio360.core.cliente.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "cliente")
public class Cliente {

  @Id
  @UuidGenerator
  @Column(name = "id", columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "empresa_id", nullable = false, columnDefinition = "BINARY(16)")
  private UUID empresaId;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_pessoa", nullable = false, length = 10)
  private TipoPessoa tipoPessoa;

  @Column(nullable = false, length = 150)
  private String nome;

  @Column(nullable = false, length = 14)
  private String documento;

  @Column(length = 160)
  private String email;

  @Column(length = 11)
  private String telefone;

  @Column(nullable = false)
  private boolean ativo;

  @Column(name = "criado_em", nullable = false, columnDefinition = "DATETIME(6)")
  private Instant criadoEm;

  @Column(name = "atualizado_em", nullable = false, columnDefinition = "DATETIME(6)")
  private Instant atualizadoEm;

  @Version
  @Column(nullable = false)
  private long versao;

  protected Cliente() {}

  public Cliente(
      UUID empresaId,
      TipoPessoa tipoPessoa,
      String nome,
      String documento,
      String email,
      String telefone,
      Instant agora) {
    this.empresaId = empresaId;
    this.tipoPessoa = tipoPessoa;
    this.nome = nome;
    this.documento = documento;
    this.email = email;
    this.telefone = telefone;
    this.ativo = true;
    this.criadoEm = agora;
    this.atualizadoEm = agora;
  }

  public void atualizar(
      TipoPessoa tipoPessoa,
      String nome,
      String documento,
      String email,
      String telefone,
      Instant agora) {
    this.tipoPessoa = tipoPessoa;
    this.nome = nome;
    this.documento = documento;
    this.email = email;
    this.telefone = telefone;
    this.atualizadoEm = agora;
  }

  public void alterarSituacao(boolean ativo, Instant agora) {
    if (this.ativo != ativo) {
      this.ativo = ativo;
      this.atualizadoEm = agora;
    }
  }

  public UUID getId() {
    return id;
  }

  public UUID getEmpresaId() {
    return empresaId;
  }

  public TipoPessoa getTipoPessoa() {
    return tipoPessoa;
  }

  public String getNome() {
    return nome;
  }

  public String getDocumento() {
    return documento;
  }

  public String getEmail() {
    return email;
  }

  public String getTelefone() {
    return telefone;
  }

  public boolean isAtivo() {
    return ativo;
  }

  public Instant getCriadoEm() {
    return criadoEm;
  }

  public Instant getAtualizadoEm() {
    return atualizadoEm;
  }

  public long getVersao() {
    return versao;
  }
}
