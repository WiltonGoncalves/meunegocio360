package br.com.meunegocio360.core.auth.application;

import br.com.meunegocio360.core.auth.domain.AuthPolicy;
import br.com.meunegocio360.core.auth.domain.RefreshToken;
import br.com.meunegocio360.core.auth.domain.Usuario;
import br.com.meunegocio360.core.auth.domain.exception.InvalidRefreshTokenException;
import br.com.meunegocio360.core.auth.domain.port.AccessTokenIssuer;
import br.com.meunegocio360.core.auth.domain.port.AccessTokenIssuer.AccessToken;
import br.com.meunegocio360.core.auth.domain.port.RefreshTokenCodec;
import br.com.meunegocio360.core.auth.domain.port.RefreshTokenRepository;
import br.com.meunegocio360.core.auth.domain.port.UsuarioRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshUseCase {

  private static final Logger LOGGER = LoggerFactory.getLogger(RefreshUseCase.class);

  private final UsuarioRepository usuarioRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final RefreshTokenCodec refreshTokenCodec;
  private final AccessTokenIssuer accessTokenIssuer;
  private final AuthPolicy policy;
  private final Clock clock;

  public RefreshUseCase(
      UsuarioRepository usuarioRepository,
      RefreshTokenRepository refreshTokenRepository,
      RefreshTokenCodec refreshTokenCodec,
      AccessTokenIssuer accessTokenIssuer,
      AuthPolicy policy,
      Clock clock) {
    this.usuarioRepository = usuarioRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.refreshTokenCodec = refreshTokenCodec;
    this.accessTokenIssuer = accessTokenIssuer;
    this.policy = policy;
    this.clock = clock;
  }

  @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
  public AuthSession executar(String tokenEmCookie) {
    if (tokenEmCookie == null || tokenEmCookie.isBlank()) {
      throw new InvalidRefreshTokenException();
    }

    String tokenHash = refreshTokenCodec.hash(tokenEmCookie);
    RefreshToken snapshot =
        refreshTokenRepository
            .buscarPorHash(tokenHash)
            .orElseThrow(InvalidRefreshTokenException::new);
    Usuario usuario =
        usuarioRepository
            .buscarPorIdParaAtualizacao(snapshot.usuarioId())
            .orElseThrow(InvalidRefreshTokenException::new);
    RefreshToken atual =
        refreshTokenRepository
            .buscarPorHashParaAtualizacao(tokenHash)
            .orElseThrow(InvalidRefreshTokenException::new);
    Instant agora = clock.instant();

    if (atual.revogado()) {
      refreshTokenRepository.revogarFamilia(atual.familiaId(), agora);
      LOGGER
          .atWarn()
          .addKeyValue("event", "auth.refresh_reuse")
          .log("Reuso de refresh token detectado");
      throw new InvalidRefreshTokenException();
    }
    if (atual.expirado(agora) || !usuario.ativo() || usuario.estaBloqueado(agora)) {
      refreshTokenRepository.revogarFamilia(atual.familiaId(), agora);
      throw new InvalidRefreshTokenException();
    }

    Usuario usuarioAtual = usuario.limparBloqueioExpirado(agora);
    if (usuarioAtual != usuario) {
      usuarioAtual = usuarioRepository.salvar(usuarioAtual);
    }
    refreshTokenRepository.revogarFamilia(atual.familiaId(), agora);

    String novoRefreshToken = refreshTokenCodec.gerar();
    refreshTokenRepository.salvar(
        new RefreshToken(
            UUID.randomUUID(),
            usuarioAtual.id(),
            atual.familiaId(),
            refreshTokenCodec.hash(novoRefreshToken),
            agora.plus(policy.duracaoRefreshToken()),
            null,
            agora));
    AccessToken novoAccessToken = accessTokenIssuer.emitir(usuarioAtual, agora);
    LOGGER.atInfo().addKeyValue("event", "auth.refresh_success").log("Sessão renovada");
    return new AuthSession(
        novoAccessToken.valor(),
        novoAccessToken.expiraEmSegundos(),
        novoRefreshToken,
        LoginUseCase.resumir(usuarioAtual));
  }
}
