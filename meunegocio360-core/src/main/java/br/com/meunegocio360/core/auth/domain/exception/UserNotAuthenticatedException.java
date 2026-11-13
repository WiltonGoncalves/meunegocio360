package br.com.meunegocio360.core.auth.domain.exception;

public class UserNotAuthenticatedException extends RuntimeException {

  public UserNotAuthenticatedException() {
    super("Não autenticado.");
  }
}
