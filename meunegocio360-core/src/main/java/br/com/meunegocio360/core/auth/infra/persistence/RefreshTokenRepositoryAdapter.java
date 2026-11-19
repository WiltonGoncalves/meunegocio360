package br.com.meunegocio360.core.auth.infra.persistence;

import br.com.meunegocio360.core.auth.domain.RefreshToken;
import br.com.meunegocio360.core.auth.domain.port.RefreshTokenRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

  private final SpringDataRefreshTokenRepository repository;

  public RefreshTokenRepositoryAdapter(SpringDataRefreshTokenRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<RefreshToken> buscarPorHash(String tokenHash) {
    return repository.findByTokenHash(tokenHash).map(RefreshTokenRepositoryAdapter::toDomain);
  }

  @Override
  public Optional<RefreshToken> buscarPorHashParaAtualizacao(String tokenHash) {
    return repository
        .findByTokenHashForUpdate(tokenHash)
        .map(RefreshTokenRepositoryAdapter::toDomain);
  }

  @Override
  public void salvar(RefreshToken refreshToken) {
    repository.saveAndFlush(
        new RefreshTokenJpaEntity(
            refreshToken.id(),
            refreshToken.usuarioId(),
            refreshToken.familiaId(),
            refreshToken.tokenHash(),
            refreshToken.expiraEm(),
            refreshToken.revogadoEm(),
            refreshToken.criadoEm()));
  }

  @Override
  public void revogarFamilia(UUID familiaId, Instant revogadoEm) {
    repository.revokeFamily(familiaId, revogadoEm);
  }

  @Override
  public void revogarTodosDoUsuario(UUID usuarioId, Instant revogadoEm) {
    repository.revokeAllForUser(usuarioId, revogadoEm);
  }

  @Override
  public void removerExpiradosOuRevogadosAntesDe(Instant limite) {
    repository.deleteOldTokens(limite);
  }

  private static RefreshToken toDomain(RefreshTokenJpaEntity entity) {
    return new RefreshToken(
        entity.getId(),
        entity.getUsuarioId(),
        entity.getFamiliaId(),
        entity.getTokenHash(),
        entity.getExpiraEm(),
        entity.getRevogadoEm(),
        entity.getCriadoEm());
  }
}
