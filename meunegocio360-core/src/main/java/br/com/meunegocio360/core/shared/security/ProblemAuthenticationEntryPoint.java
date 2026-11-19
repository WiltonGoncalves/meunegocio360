package br.com.meunegocio360.core.shared.security;

import br.com.meunegocio360.core.shared.api.ApiProblemFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class ProblemAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ApiProblemFactory problemFactory;
  private final JsonMapper jsonMapper;

  public ProblemAuthenticationEntryPoint(ApiProblemFactory problemFactory, JsonMapper jsonMapper) {
    this.problemFactory = problemFactory;
    this.jsonMapper = jsonMapper;
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException, ServletException {
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    response.setHeader("WWW-Authenticate", "Bearer");
    jsonMapper.writeValue(
        response.getOutputStream(),
        problemFactory.create(
            HttpStatus.UNAUTHORIZED,
            "UNAUTHORIZED",
            "Não autenticado",
            "Autenticação necessária ou token inválido.",
            new org.springframework.web.context.request.ServletWebRequest(request)));
  }
}
