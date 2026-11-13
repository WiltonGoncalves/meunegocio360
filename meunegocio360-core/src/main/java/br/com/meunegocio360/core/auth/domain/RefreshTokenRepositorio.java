package br.com.meunegocio360.core.auth.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepositorio {

  Optional<RefreshToken> buscarPorHashParaAtualizacao(String hash);

  RefreshToken salvar(RefreshToken token);

  void revogarFamilia(UUID familiaId, Instant agora);

  void revogarTodasDoUsuario(UUID usuarioId, Instant agora);

  void removerExpiradosOuRevogadosAntesDe(Instant limite);
}
