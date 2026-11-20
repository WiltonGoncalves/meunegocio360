package br.com.meunegocio360.core.empresa.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "empresa")
public class Empresa {

  @Id
  @UuidGenerator
  @Column(name = "id", columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "razao_social", nullable = false, length = 150)
  private String razaoSocial;

  @Column(name = "nome_fantasia", length = 150)
  private String nomeFantasia;

  @Column(nullable = false, unique = true, length = 14)
  private String cnpj;

  @Column(length = 160)
  private String email;

  @Column(length = 11)
  private String telefone;

  @Column(name = "fuso_horario", nullable = false, length = 50)
  private String fusoHorario;

  @Column(nullable = false, length = 3)
  private String moeda;

  @Column(nullable = false)
  private boolean ativa;

  @Column(name = "criado_em", nullable = false, columnDefinition = "DATETIME(6)")
  private Instant criadoEm;

  @Column(name = "atualizado_em", nullable = false, columnDefinition = "DATETIME(6)")
  private Instant atualizadoEm;

  @Version
  @Column(nullable = false)
  private long versao;

  protected Empresa() {}

  public Empresa(
      String razaoSocial,
      String nomeFantasia,
      String cnpj,
      String email,
      String telefone,
      String fusoHorario,
      String moeda,
      Instant agora) {
    this.razaoSocial = razaoSocial;
    this.nomeFantasia = nomeFantasia;
    this.cnpj = cnpj;
    this.email = email;
    this.telefone = telefone;
    this.fusoHorario = fusoHorario;
    this.moeda = moeda;
    this.ativa = true;
    this.criadoEm = agora;
    this.atualizadoEm = agora;
  }

  public void atualizar(
      String razaoSocial,
      String nomeFantasia,
      String cnpj,
      String email,
      String telefone,
      String fusoHorario,
      String moeda,
      Instant agora) {
    this.razaoSocial = razaoSocial;
    this.nomeFantasia = nomeFantasia;
    this.cnpj = cnpj;
    this.email = email;
    this.telefone = telefone;
    this.fusoHorario = fusoHorario;
    this.moeda = moeda;
    this.atualizadoEm = agora;
  }

  public void alterarSituacao(boolean ativa, Instant agora) {
    if (this.ativa != ativa) {
      this.ativa = ativa;
      this.atualizadoEm = agora;
    }
  }

  public UUID getId() {
    return id;
  }

  public String getRazaoSocial() {
    return razaoSocial;
  }

  public String getNomeFantasia() {
    return nomeFantasia;
  }

  public String getCnpj() {
    return cnpj;
  }

  public String getEmail() {
    return email;
  }

  public String getTelefone() {
    return telefone;
  }

  public String getFusoHorario() {
    return fusoHorario;
  }

  public String getMoeda() {
    return moeda;
  }

  public boolean isAtiva() {
    return ativa;
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
