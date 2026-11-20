package br.com.meunegocio360.core.cliente.api;

import br.com.meunegocio360.core.cliente.domain.TipoPessoa;
import br.com.meunegocio360.core.shared.validation.ValidadorCnpj;
import br.com.meunegocio360.core.shared.validation.ValidadorCpf;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class DocumentoClienteValidoValidator
    implements ConstraintValidator<DocumentoClienteValido, Object> {

  private static final Pattern CPF_FORMATADO = Pattern.compile("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}");
  private static final Pattern CNPJ_FORMATADO =
      Pattern.compile("\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}");

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    if (!(value instanceof DadosDocumento dados)
        || dados.documento() == null
        || dados.tipoPessoa() == null) {
      return true;
    }

    String documento = dados.documento().trim();
    boolean formatoValido =
        documento.matches("\\d+")
            || CPF_FORMATADO.matcher(documento).matches()
            || CNPJ_FORMATADO.matcher(documento).matches();
    String digitos = documento.replaceAll("\\D", "");
    boolean documentoValido =
        formatoValido
            && switch (dados.tipoPessoa()) {
              case FISICA -> ValidadorCpf.valido(digitos);
              case JURIDICA -> ValidadorCnpj.valido(digitos);
            };
    if (!documentoValido) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
          .addPropertyNode("documento")
          .addConstraintViolation();
    }
    return documentoValido;
  }

  public interface DadosDocumento {

    TipoPessoa tipoPessoa();

    String documento();
  }
}
