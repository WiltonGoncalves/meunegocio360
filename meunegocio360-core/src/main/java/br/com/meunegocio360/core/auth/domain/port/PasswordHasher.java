package br.com.meunegocio360.core.auth.domain.port;

public interface PasswordHasher {

  String hash(String senha);

  boolean corresponde(String senha, String hash);
}
