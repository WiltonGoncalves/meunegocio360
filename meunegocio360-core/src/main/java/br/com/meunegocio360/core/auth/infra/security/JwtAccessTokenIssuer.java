package br.com.meunegocio360.core.auth.infra.security;

import br.com.meunegocio360.core.auth.domain.AuthPolicy;
import br.com.meunegocio360.core.auth.domain.Usuario;
import br.com.meunegocio360.core.auth.domain.port.AccessTokenIssuer;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
public class JwtAccessTokenIssuer implements AccessTokenIssuer {

  private final JwtEncoder jwtEncoder;
  private final AuthPolicy policy;

  public JwtAccessTokenIssuer(JwtEncoder jwtEncoder, AuthPolicy policy) {
    this.jwtEncoder = jwtEncoder;
    this.policy = policy;
  }

  @Override
  public AccessToken emitir(Usuario usuario, Instant emitidoEm) {
    Instant expiraEm = emitidoEm.plus(policy.duracaoAccessToken());
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer("meunegocio360-core")
            .subject(usuario.id().toString())
            .issuedAt(emitidoEm)
            .expiresAt(expiraEm)
            .id(UUID.randomUUID().toString())
            .claim("perfil", usuario.perfil().name())
            .build();
    String token =
        jwtEncoder
            .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
            .getTokenValue();
    return new AccessToken(token, policy.duracaoAccessToken().toSeconds());
  }
}
