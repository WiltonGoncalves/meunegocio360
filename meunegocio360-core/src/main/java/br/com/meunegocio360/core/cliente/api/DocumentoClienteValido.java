package br.com.meunegocio360.core.cliente.api;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = DocumentoClienteValidoValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocumentoClienteValido {

  String message() default
      "não corresponde ao tipo de pessoa ou possui dígitos verificadores inválidos";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
