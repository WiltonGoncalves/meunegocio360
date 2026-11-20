package br.com.meunegocio360.core.shared.validation;

public final class ValidadorCnpj {

  private static final int[] PESOS_PRIMEIRO_DIGITO = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
  private static final int[] PESOS_SEGUNDO_DIGITO = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

  private ValidadorCnpj() {}

  public static boolean valido(String cnpj) {
    if (cnpj == null || !cnpj.matches("\\d{14}") || cnpj.chars().distinct().count() == 1) {
      return false;
    }

    int primeiroDigito = calcularDigito(cnpj, PESOS_PRIMEIRO_DIGITO, 12);
    int segundoDigito = calcularDigito(cnpj, PESOS_SEGUNDO_DIGITO, 13);
    return cnpj.charAt(12) - '0' == primeiroDigito && cnpj.charAt(13) - '0' == segundoDigito;
  }

  private static int calcularDigito(String cnpj, int[] pesos, int limite) {
    int soma = 0;
    for (int indice = 0; indice < limite; indice++) {
      soma += (cnpj.charAt(indice) - '0') * pesos[indice];
    }
    int resto = soma % 11;
    return resto < 2 ? 0 : 11 - resto;
  }
}
