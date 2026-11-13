package br.com.meunegocio360.core.auth.domain.port;

import br.com.meunegocio360.core.auth.domain.Usuario;
import java.time.Instant;

public interface AccessTokenIssuer {

  AccessToken emitir(Usuario usuario, Instant emitidoEm);

  record AccessToken(String valor, long expiraEmSegundos) {}
}
