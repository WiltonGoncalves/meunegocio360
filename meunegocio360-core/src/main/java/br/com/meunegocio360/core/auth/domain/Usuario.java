package br.com.meunegocio360.core.auth.domain;

import java.time.Instant;
import java.util.UUID;

public record Usuario(
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

  public boolean estaBloqueado(Instant agora) {
    return bloqueadoAte != null && agora.isBefore(bloqueadoAte);
  }

  public Usuario registrarFalha(Instant agora, AuthPolicy policy) {
    int falhas = tentativasFalhas + 1;
    Instant bloqueio =
        falhas >= policy.maxTentativas() ? agora.plus(policy.duracaoBloqueio()) : bloqueadoAte;
    return new Usuario(
        id,
        nome,
        email,
        senhaHash,
        perfil,
        ativo,
        falhas,
        bloqueio,
        ultimoLoginEm,
        criadoEm,
        agora,
        versao);
  }

  public Usuario registrarSucesso(Instant agora) {
    return new Usuario(
        id, nome, email, senhaHash, perfil, ativo, 0, null, agora, criadoEm, agora, versao);
  }

  public Usuario limparBloqueioExpirado(Instant agora) {
    if (bloqueadoAte == null || agora.isBefore(bloqueadoAte)) {
      return this;
    }
    return new Usuario(
        id, nome, email, senhaHash, perfil, ativo, 0, null, ultimoLoginEm, criadoEm, agora, versao);
  }
}
