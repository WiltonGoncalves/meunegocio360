package br.com.meunegocio360.core.empresa.api;

import br.com.meunegocio360.core.shared.validation.CnpjValido;
import br.com.meunegocio360.core.shared.validation.FusoHorarioIana;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CriarEmpresaRequest(
    @NotBlank(message = "é obrigatório")
        @Size(min = 3, max = 150, message = "deve conter de 3 a 150 caracteres")
        String razaoSocial,
    @Size(max = 150, message = "deve conter no máximo 150 caracteres") String nomeFantasia,
    @NotBlank(message = "é obrigatório") @CnpjValido String cnpj,
    @Email(message = "deve ser um e-mail válido")
        @Size(max = 160, message = "deve conter no máximo 160 caracteres")
        String email,
    @Pattern(regexp = "\\d{10,11}", message = "deve conter 10 ou 11 dígitos") String telefone,
    @FusoHorarioIana String fusoHorario,
    @Pattern(regexp = "BRL", message = "nesta versão, deve ser BRL") String moeda) {

  public CriarEmpresaRequest {
    razaoSocial = trimObrigatorio(razaoSocial);
    nomeFantasia = trimOpcional(nomeFantasia);
    cnpj = trimObrigatorio(cnpj);
    email = trimOpcional(email);
    telefone = trimOpcional(telefone);
    fusoHorario =
        fusoHorario == null || fusoHorario.isBlank() ? "America/Sao_Paulo" : fusoHorario.trim();
    moeda = moeda == null || moeda.isBlank() ? "BRL" : moeda.trim();
  }

  private static String trimObrigatorio(String valor) {
    return valor == null ? null : valor.trim();
  }

  private static String trimOpcional(String valor) {
    String ajustado = trimObrigatorio(valor);
    return ajustado == null || ajustado.isEmpty() ? null : ajustado;
  }
}
