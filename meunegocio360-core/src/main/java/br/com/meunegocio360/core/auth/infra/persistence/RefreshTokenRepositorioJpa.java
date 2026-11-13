package br.com.meunegocio360.core.auth.infra.persistence;

import br.com.meunegocio360.core.auth.domain.RefreshToken;
import br.com.meunegocio360.core.auth.domain.RefreshTokenRepositorio;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class RefreshTokenRepositorioJpa implements RefreshTokenRepositorio {

  private final SpringDataRefreshTokenRepository repository;

  public RefreshTokenRepositorioJpa(SpringDataRefreshTokenRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<RefreshToken> buscarPorHashParaAtualizacao(String hash) {
    return repository.buscarPorHashParaAtualizacao(hash);
  }

  @Override
  public RefreshToken salvar(RefreshToken token) {
    return repository.save(token);
  }

  @Override
  public void revogarFamilia(UUID familiaId, Instant agora) {
    repository.revogarFamilia(familiaId, agora);
  }

  @Override
  public void revogarTodasDoUsuario(UUID usuarioId, Instant agora) {
    repository.revogarTodasDoUsuario(usuarioId, agora);
  }

  @Override
  public void removerExpiradosOuRevogadosAntesDe(Instant limite) {
    repository.removerExpiradosOuRevogadosAntesDe(limite);
  }
}
