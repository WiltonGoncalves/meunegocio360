package br.com.meunegocio360.core.auth.infra.security;

import br.com.meunegocio360.core.auth.domain.port.RefreshTokenCodec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

@Component
public class SecureRefreshTokenCodec implements RefreshTokenCodec {

  private final SecureRandom secureRandom = new SecureRandom();

  @Override
  public String gerar() {
    byte[] tokenBytes = new byte[32];
    secureRandom.nextBytes(tokenBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
  }

  @Override
  public String hash(String valor) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256")
                  .digest(valor.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is required by the Java runtime.", exception);
    }
  }
}
