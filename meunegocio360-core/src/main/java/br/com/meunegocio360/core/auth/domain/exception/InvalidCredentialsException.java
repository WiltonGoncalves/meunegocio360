package br.com.meunegocio360.core.auth.domain.exception;

public class InvalidCredentialsException extends RuntimeException {

  public InvalidCredentialsException() {
    super("E-mail ou senha inválidos.");
  }
}
