package br.com.meunegocio360.core.auth.domain.exception;

public class AccountTemporarilyLockedException extends RuntimeException {

  private final long retryAfterSeconds;

  public AccountTemporarilyLockedException(long retryAfterSeconds) {
    super("Conta temporariamente bloqueada.");
    this.retryAfterSeconds = Math.max(1, retryAfterSeconds);
  }

  public long retryAfterSeconds() {
    return retryAfterSeconds;
  }
}
