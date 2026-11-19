package br.com.meunegocio360.core.auth.domain.port;

public interface RefreshTokenCodec {

  String gerar();

  String hash(String valor);
}
