package br.com.meunegocio360.core.auth.api;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshCookie {

  public static final String NAME = "mn360_refresh";
  private static final String PATH = "/api/v1/auth";

  private final boolean secure;

  public RefreshCookie(@Value("${app.auth.cookie.secure:true}") boolean secure) {
    this.secure = secure;
  }

  public String set(String token, long maxAgeSeconds) {
    return ResponseCookie.from(NAME, token)
        .httpOnly(true)
        .secure(secure)
        .sameSite("Strict")
        .path(PATH)
        .maxAge(Duration.ofSeconds(maxAgeSeconds))
        .build()
        .toString();
  }

  public String clear() {
    return ResponseCookie.from(NAME, "")
        .httpOnly(true)
        .secure(secure)
        .sameSite("Strict")
        .path(PATH)
        .maxAge(Duration.ZERO)
        .build()
        .toString();
  }

  public static void adicionarCookie(HttpHeaders headers, String cookie) {
    headers.add(HttpHeaders.SET_COOKIE, cookie);
  }
}
