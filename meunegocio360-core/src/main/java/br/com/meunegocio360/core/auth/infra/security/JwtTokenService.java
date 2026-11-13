package br.com.meunegocio360.core.auth.infra.security;

import br.com.meunegocio360.core.auth.domain.TokenService;
import br.com.meunegocio360.core.auth.domain.Usuario;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenService implements TokenService {

  private final JwtEncoder jwtEncoder;
  private final SecureRandom secureRandom;
  private final Duration accessTokenDuration;
  private final Duration refreshTokenDuration;

  public JwtTokenService(
      JwtEncoder jwtEncoder,
      SecureRandom secureRandom,
      @Value("${app.auth.jwt.expiracao-minutos:15}") long accessTokenMinutes,
      @Value("${app.auth.refresh.expiracao-dias:7}") long refreshTokenDays) {
    this.jwtEncoder = jwtEncoder;
    this.secureRandom = secureRandom;
    this.accessTokenDuration = Duration.ofMinutes(accessTokenMinutes);
    this.refreshTokenDuration = Duration.ofDays(refreshTokenDays);
  }

  @Override
  public String emitirAcesso(Usuario usuario, Instant agora) {
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer("meunegocio360-core")
            .subject(usuario.getId().toString())
            .issuedAt(agora)
            .expiresAt(agora.plus(accessTokenDuration))
            .id(UUID.randomUUID().toString())
            .claim("perfil", usuario.getPerfil())
            .build();
    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
    return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
  }

  @Override
  public String novoRefresh() {
    byte[] valor = new byte[32];
    secureRandom.nextBytes(valor);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(valor);
  }

  @Override
  public String hashRefresh(String token) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.US_ASCII));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 não está disponível.", exception);
    }
  }
}
