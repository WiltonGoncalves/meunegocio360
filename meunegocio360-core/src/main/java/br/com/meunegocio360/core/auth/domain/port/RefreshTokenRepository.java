package br.com.meunegocio360.core.auth.domain.port;

import br.com.meunegocio360.core.auth.domain.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {

  Optional<RefreshToken> buscarPorHash(String tokenHash);

  Optional<RefreshToken> buscarPorHashParaAtualizacao(String tokenHash);

  void salvar(RefreshToken refreshToken);

  void revogarFamilia(UUID familiaId, Instant revogadoEm);

  void revogarTodosDoUsuario(UUID usuarioId, Instant revogadoEm);

  void removerExpiradosOuRevogadosAntesDe(Instant limite);
}
