package br.com.meunegocio360.core.auth.application;

import br.com.meunegocio360.core.auth.domain.RefreshToken;
import br.com.meunegocio360.core.auth.domain.port.RefreshTokenCodec;
import br.com.meunegocio360.core.auth.domain.port.RefreshTokenRepository;
import br.com.meunegocio360.core.auth.domain.port.UsuarioRepository;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogoutUseCase {

  private final UsuarioRepository usuarioRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final RefreshTokenCodec refreshTokenCodec;
  private final Clock clock;

  public LogoutUseCase(
      UsuarioRepository usuarioRepository,
      RefreshTokenRepository refreshTokenRepository,
      RefreshTokenCodec refreshTokenCodec,
      Clock clock) {
    this.usuarioRepository = usuarioRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.refreshTokenCodec = refreshTokenCodec;
    this.clock = clock;
  }

  @Transactional
  public void executar(String tokenEmCookie) {
    if (tokenEmCookie == null || tokenEmCookie.isBlank()) {
      return;
    }
    String hash = refreshTokenCodec.hash(tokenEmCookie);
    RefreshToken snapshot = refreshTokenRepository.buscarPorHash(hash).orElse(null);
    if (snapshot == null) {
      return;
    }
    if (usuarioRepository.buscarPorIdParaAtualizacao(snapshot.usuarioId()).isEmpty()) {
      return;
    }
    RefreshToken atual = refreshTokenRepository.buscarPorHashParaAtualizacao(hash).orElse(null);
    if (atual != null) {
      Instant agora = clock.instant();
      refreshTokenRepository.revogarFamilia(atual.familiaId(), agora);
    }
  }
}
