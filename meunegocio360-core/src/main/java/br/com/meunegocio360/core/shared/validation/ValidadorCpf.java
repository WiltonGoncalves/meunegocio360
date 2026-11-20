package br.com.meunegocio360.core.shared.validation;

public final class ValidadorCpf {

  private ValidadorCpf() {}

  public static boolean valido(String cpf) {
    if (cpf == null || !cpf.matches("\\d{11}") || cpf.chars().distinct().count() == 1) {
      return false;
    }

    int primeiroDigito = calcularDigito(cpf, 9);
    int segundoDigito = calcularDigito(cpf, 10);
    return cpf.charAt(9) - '0' == primeiroDigito && cpf.charAt(10) - '0' == segundoDigito;
  }

  private static int calcularDigito(String cpf, int limite) {
    int soma = 0;
    for (int indice = 0; indice < limite; indice++) {
      soma += (cpf.charAt(indice) - '0') * (limite + 1 - indice);
    }
    int resto = soma % 11;
    return resto < 2 ? 0 : 11 - resto;
  }
}
