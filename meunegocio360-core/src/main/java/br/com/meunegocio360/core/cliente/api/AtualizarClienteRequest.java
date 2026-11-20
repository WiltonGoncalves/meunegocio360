package br.com.meunegocio360.core.cliente.api;

import br.com.meunegocio360.core.cliente.domain.TipoPessoa;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@DocumentoClienteValido
public record AtualizarClienteRequest(
    @NotNull(message = "é obrigatório") TipoPessoa tipoPessoa,
    @NotBlank(message = "é obrigatório")
        @Size(min = 2, max = 150, message = "deve conter de 2 a 150 caracteres")
        String nome,
    @NotBlank(message = "é obrigatório") String documento,
    @Email(message = "deve ser um e-mail válido")
        @Size(max = 160, message = "deve conter no máximo 160 caracteres")
        String email,
    @Pattern(regexp = "\\d{10,11}", message = "deve conter 10 ou 11 dígitos") String telefone,
    @NotNull(message = "é obrigatório") @PositiveOrZero(message = "não pode ser negativo")
        Long versao)
    implements DocumentoClienteValidoValidator.DadosDocumento {

  public AtualizarClienteRequest {
    nome = trim(nome);
    documento = trim(documento);
    email = trimOpcional(email);
    telefone = trimOpcional(telefone);
  }

  private static String trim(String value) {
    return value == null ? null : value.trim();
  }

  private static String trimOpcional(String value) {
    String trimmed = trim(value);
    return trimmed == null || trimmed.isEmpty() ? null : trimmed;
  }
}
