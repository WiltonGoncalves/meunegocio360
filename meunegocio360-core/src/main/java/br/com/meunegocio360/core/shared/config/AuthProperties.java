package br.com.meunegocio360.core.shared.config;

import java.util.Base64;
import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
    int maxTentativas, int bloqueioMinutos, Jwt jwt, Refresh refresh, Cookie cookie) {

  public AuthProperties {
    Objects.requireNonNull(jwt);
    Objects.requireNonNull(refresh);
    Objects.requireNonNull(cookie);
    if (maxTentativas < 1
        || bloqueioMinutos < 1
        || jwt.expiracaoMinutos < 1
        || refresh.expiracaoDias < 1) {
      throw new IllegalArgumentException("Auth expiration and lockout settings must be positive.");
    }
    byte[] secretBytes;
    try {
      secretBytes = Base64.getDecoder().decode(jwt.secret);
    } catch (IllegalArgumentException exception) {
      throw new IllegalArgumentException(
          "JWT_SECRET must be valid Base64 containing at least 32 bytes.");
    }
    if (secretBytes.length < 32) {
      throw new IllegalArgumentException(
          "JWT_SECRET must be valid Base64 containing at least 32 bytes.");
    }
  }

  public record Jwt(String secret, int expiracaoMinutos) {
    public Jwt {
      Objects.requireNonNull(secret);
    }
  }

  public record Refresh(int expiracaoDias) {}

  public record Cookie(boolean secure) {}
}
