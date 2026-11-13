package br.com.meunegocio360.core.auth.infra.security;

import br.com.meunegocio360.core.auth.domain.port.PasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DelegatingPasswordHasher implements PasswordHasher {

  private final PasswordEncoder passwordEncoder;

  public DelegatingPasswordHasher(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public String hash(String senha) {
    return passwordEncoder.encode(senha);
  }

  @Override
  public boolean corresponde(String senha, String hash) {
    return passwordEncoder.matches(senha, hash);
  }
}
