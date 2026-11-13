package br.com.meunegocio360.core.auth.domain;

public interface CredencialService {

  String codificar(String senha);

  boolean corresponde(String senha, String senhaCodificada);
}
