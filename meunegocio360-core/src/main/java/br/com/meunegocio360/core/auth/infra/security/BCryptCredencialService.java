package br.com.meunegocio360.core.auth.infra.security;

import br.com.meunegocio360.core.auth.domain.CredencialService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptCredencialService implements CredencialService {

  private final PasswordEncoder passwordEncoder;

  public BCryptCredencialService(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public String codificar(String senha) {
    return passwordEncoder.encode(senha);
  }

  @Override
  public boolean corresponde(String senha, String senhaCodificada) {
    return passwordEncoder.matches(senha, senhaCodificada);
  }
}
