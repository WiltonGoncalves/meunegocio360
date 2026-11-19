package br.com.meunegocio360.core.auth.api;

import br.com.meunegocio360.core.shared.api.ApiProblemFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class AuthOriginFilter extends OncePerRequestFilter {

  private static final String CODE = "ORIGIN_NOT_ALLOWED";

  private final Set<String> allowedOrigins;
  private final ApiProblemFactory problemFactory;
  private final JsonMapper jsonMapper;

  public AuthOriginFilter(
      @Value("${cors.allowed-origins}") String origins,
      ApiProblemFactory problemFactory,
      JsonMapper jsonMapper) {
    this.allowedOrigins =
        Arrays.stream(origins.split(","))
            .map(String::trim)
            .filter(origin -> !origin.isEmpty())
            .collect(Collectors.toUnmodifiableSet());
    this.problemFactory = problemFactory;
    this.jsonMapper = jsonMapper;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String path = request.getRequestURI().substring(request.getContextPath().length());
    String origin = request.getHeader("Origin");
    boolean refreshOrLogout =
        "POST".equals(request.getMethod())
            && (path.equals("/api/v1/auth/refresh") || path.equals("/api/v1/auth/logout"));
    if (refreshOrLogout && origin != null && !allowedOrigins.contains(origin)) {
      response.setStatus(HttpStatus.FORBIDDEN.value());
      response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
      response.setCharacterEncoding(StandardCharsets.UTF_8.name());
      jsonMapper.writeValue(
          response.getOutputStream(),
          problemFactory.create(
              HttpStatus.FORBIDDEN,
              CODE,
              "Origem não permitida",
              "A origem desta requisição não é permitida.",
              new ServletWebRequest(request)));
      return;
    }

    filterChain.doFilter(request, response);
  }
}
