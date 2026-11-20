package br.com.meunegocio360.core.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CnpjValidoValidator implements ConstraintValidator<CnpjValido, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return value == null || value.isBlank() || ValidadorCnpj.valido(value);
  }
}
