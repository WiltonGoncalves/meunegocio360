package br.com.meunegocio360.core.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.ZoneId;

public class FusoHorarioIanaValidator implements ConstraintValidator<FusoHorarioIana, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return value == null || value.isBlank() || ZoneId.getAvailableZoneIds().contains(value);
  }
}
