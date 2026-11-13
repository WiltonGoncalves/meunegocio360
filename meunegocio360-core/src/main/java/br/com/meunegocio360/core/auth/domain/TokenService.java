package br.com.meunegocio360.core.auth.domain;

import java.time.Instant;

public interface TokenService {

  String emitirAcesso(Usuario usuario, Instant agora);

  String novoRefresh();

  String hashRefresh(String token);
}
