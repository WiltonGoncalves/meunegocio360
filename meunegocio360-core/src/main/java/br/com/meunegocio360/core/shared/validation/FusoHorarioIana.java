package br.com.meunegocio360.core.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = FusoHorarioIanaValidator.class)
@Target({
  ElementType.FIELD,
  ElementType.PARAMETER,
  ElementType.ANNOTATION_TYPE,
  ElementType.RECORD_COMPONENT
})
@Retention(RetentionPolicy.RUNTIME)
public @interface FusoHorarioIana {

  String message() default "deve ser um identificador de fuso horário IANA válido";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
