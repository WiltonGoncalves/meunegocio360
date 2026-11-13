package br.com.meunegocio360.core.shared.error;

public class ErroAutenticacao extends RuntimeException {

  private final int status;
  private final String code;
  private final String title;
  private final Long retryAfterSeconds;

  public ErroAutenticacao(
      int status, String code, String title, String detail, Long retryAfterSeconds) {
    super(detail);
    this.status = status;
    this.code = code;
    this.title = title;
    this.retryAfterSeconds = retryAfterSeconds;
  }

  public int getStatus() {
    return status;
  }

  public String getCode() {
    return code;
  }

  public String getTitle() {
    return title;
  }

  public Long getRetryAfterSeconds() {
    return retryAfterSeconds;
  }

  public static ErroAutenticacao credenciaisInvalidas() {
    return new ErroAutenticacao(
        401, "INVALID_CREDENTIALS", "Não autenticado", "E-mail ou senha inválidos.", null);
  }

  public static ErroAutenticacao refreshInvalido() {
    return new ErroAutenticacao(
        401,
        "INVALID_REFRESH_TOKEN",
        "Não autenticado",
        "Token de renovação inválido ou expirado.",
        null);
  }

  public static ErroAutenticacao naoAutorizado() {
    return new ErroAutenticacao(
        401,
        "UNAUTHORIZED",
        "Não autenticado",
        "Autenticação necessária ou token de acesso inválido.",
        null);
  }

  public static ErroAutenticacao contaBloqueada(long retryAfterSeconds) {
    return new ErroAutenticacao(
        429,
        "ACCOUNT_TEMPORARILY_LOCKED",
        "Conta temporariamente bloqueada",
        "Tente novamente após o período de bloqueio.",
        retryAfterSeconds);
  }

  public static ErroAutenticacao origemNaoPermitida() {
    return new ErroAutenticacao(
        403,
        "ORIGIN_NOT_ALLOWED",
        "Acesso negado",
        "A origem da requisição não é permitida.",
        null);
  }
}
