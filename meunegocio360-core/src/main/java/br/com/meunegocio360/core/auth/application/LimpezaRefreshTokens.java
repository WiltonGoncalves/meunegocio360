package br.com.meunegocio360.core.auth.application;

import br.com.meunegocio360.core.auth.domain.RefreshTokenRepositorio;
import java.time.Clock;
import java.time.Duration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LimpezaRefreshTokens {

  private final RefreshTokenRepositorio refreshTokenRepositorio;
  private final Clock clock;

  public LimpezaRefreshTokens(RefreshTokenRepositorio refreshTokenRepositorio, Clock clock) {
    this.refreshTokenRepositorio = refreshTokenRepositorio;
    this.clock = clock;
  }

  @Scheduled(cron = "0 0 3 * * *", zone = "UTC")
  @Transactional
  public void removerTokensAntigos() {
    refreshTokenRepositorio.removerExpiradosOuRevogadosAntesDe(
        clock.instant().minus(Duration.ofDays(30)));
  }
}
