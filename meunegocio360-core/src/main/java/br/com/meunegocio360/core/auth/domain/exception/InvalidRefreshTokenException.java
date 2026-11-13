package br.com.meunegocio360.core.auth.domain.exception;

public class InvalidRefreshTokenException extends RuntimeException {

  public InvalidRefreshTokenException() {
    super("Refresh token inválido.");
  }
}
