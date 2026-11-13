package br.com.meunegocio360.core.shared.security;

import br.com.meunegocio360.core.shared.api.ApiProblemFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class ProblemAccessDeniedHandler implements AccessDeniedHandler {

  private final ApiProblemFactory problemFactory;
  private final JsonMapper jsonMapper;

  public ProblemAccessDeniedHandler(ApiProblemFactory problemFactory, JsonMapper jsonMapper) {
    this.problemFactory = problemFactory;
    this.jsonMapper = jsonMapper;
  }

  @Override
  public void handle(
      HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception)
      throws IOException, ServletException {
    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    jsonMapper.writeValue(
        response.getOutputStream(),
        problemFactory.create(
            HttpStatus.FORBIDDEN,
            "FORBIDDEN",
            "Acesso negado",
            "Você não tem permissão para acessar este recurso.",
            new org.springframework.web.context.request.ServletWebRequest(request)));
  }
}
