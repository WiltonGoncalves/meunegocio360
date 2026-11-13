package br.com.meunegocio360.core.auth.application;

import br.com.meunegocio360.core.auth.domain.port.RefreshTokenRepository;
import java.time.Clock;
import java.time.Duration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CleanupRefreshTokens {

  private final RefreshTokenRepository refreshTokenRepository;
  private final Clock clock;

  public CleanupRefreshTokens(RefreshTokenRepository refreshTokenRepository, Clock clock) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.clock = clock;
  }

  @Scheduled(cron = "0 0 3 * * *", zone = "UTC")
  @Transactional
  public void removerRegistrosAntigos() {
    refreshTokenRepository.removerExpiradosOuRevogadosAntesDe(
        clock.instant().minus(Duration.ofDays(30)));
  }
}
