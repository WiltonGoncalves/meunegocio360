package br.com.meunegocio360.core.shared.api;

import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@Component
public class ApiProblemFactory {

  public ProblemDetail create(
      HttpStatus status, String code, String title, String detail, WebRequest webRequest) {
    return create(status, code, title, detail, webRequest, List.of());
  }

  public ProblemDetail create(
      HttpStatus status,
      String code,
      String title,
      String detail,
      WebRequest webRequest,
      List<ValidationFieldError> errors) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
    problem.setType(
        URI.create(
            "https://meunegocio360.com.br/problemas/"
                + code.toLowerCase(java.util.Locale.ROOT).replace('_', '-')));
    problem.setTitle(title);
    if (webRequest instanceof ServletWebRequest servletWebRequest) {
      problem.setInstance(URI.create(servletWebRequest.getRequest().getRequestURI()));
      Object traceId =
          servletWebRequest.getRequest().getAttribute(RequestIdFilter.TRACE_ID_ATTRIBUTE);
      problem.setProperty("traceId", traceId == null ? "" : traceId.toString());
    }
    problem.setProperty("code", code);
    if (!errors.isEmpty()) {
      problem.setProperty("errors", errors);
    }
    return problem;
  }

  public record ValidationFieldError(String field, String message) {}
}
