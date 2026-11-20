package br.com.meunegocio360.core.empresa.domain;

public class ConflitoEmpresa extends RuntimeException {

  public enum Tipo {
    CNPJ_DUPLICADO,
    EMPRESA_ATIVA
  }

  private final Tipo tipo;

  public ConflitoEmpresa(Tipo tipo) {
    this.tipo = tipo;
  }

  public Tipo getTipo() {
    return tipo;
  }
}
