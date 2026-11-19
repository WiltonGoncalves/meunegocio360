package br.com.meunegocio360.core.auth.api;

import br.com.meunegocio360.core.shared.config.AuthProperties;
import java.time.Duration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AuthCookieFactory {

  public static final String COOKIE_NAME = "mn360_refresh";
  private static final String COOKIE_PATH = "/api/v1/auth";

  private final boolean secure;
  private final Duration refreshLifetime;

  public AuthCookieFactory(AuthProperties properties, Environment environment) {
    this.secure = properties.cookie().secure();
    if (!secure && !environment.acceptsProfiles(Profiles.of("dev"))) {
      throw new IllegalArgumentException(
          "APP_AUTH_COOKIE_SECURE=false is permitted only in the dev profile.");
    }
    this.refreshLifetime =
        properties.refresh().expiracaoDias() > 0
            ? Duration.ofDays(properties.refresh().expiracaoDias())
            : Duration.ZERO;
  }

  public ResponseCookie criar(String token) {
    return ResponseCookie.from(COOKIE_NAME, token)
        .httpOnly(true)
        .secure(secure)
        .sameSite("Strict")
        .path(COOKIE_PATH)
        .maxAge(refreshLifetime)
        .build();
  }

  public ResponseCookie limpar() {
    return ResponseCookie.from(COOKIE_NAME, "")
        .httpOnly(true)
        .secure(secure)
        .sameSite("Strict")
        .path(COOKIE_PATH)
        .maxAge(Duration.ZERO)
        .build();
  }
}
