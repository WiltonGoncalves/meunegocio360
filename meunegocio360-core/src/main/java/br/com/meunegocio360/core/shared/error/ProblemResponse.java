package br.com.meunegocio360.core.shared.error;

import java.net.URI;
import java.util.List;

public record ProblemResponse(
    URI type,
    String title,
    int status,
    String detail,
    String instance,
    String code,
    String traceId,
    List<FieldValidationError> errors) {

  public record FieldValidationError(String field, String message) {}
}
