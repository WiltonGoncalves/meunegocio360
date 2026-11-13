package br.com.meunegocio360.core.auth.api;

public record LoginResponse(
    String tokenType, String accessToken, int expiresIn, UsuarioResponse usuario) {

  @Override
  public String toString() {
    return "LoginResponse[tokenType=%s, accessToken=redacted, expiresIn=%d]"
        .formatted(tokenType, expiresIn);
  }
}
