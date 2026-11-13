package br.com.meunegocio360.core.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.MDC;

public class ProblemWriter {

  private final ObjectMapper objectMapper = new ObjectMapper();

  public void write(
      HttpServletRequest request,
      HttpServletResponse response,
      int status,
      String code,
      String title,
      String detail,
      Long retryAfterSeconds,
      boolean bearerChallenge)
      throws IOException {
    String traceId = MDC.get("requestId");
    if (traceId == null) {
      traceId = request.getHeader("X-Request-Id");
    }
    Map<String, Object> problem = new LinkedHashMap<>();
    problem.put("type", URI.create("https://meunegocio360.com.br/problemas/" + code.toLowerCase()));
    problem.put("title", title);
    problem.put("status", status);
    problem.put("detail", detail);
    problem.put("instance", request.getRequestURI());
    problem.put("code", code);
    problem.put("traceId", traceId);
    response.setStatus(status);
    response.setContentType("application/problem+json");
    response.setCharacterEncoding("UTF-8");
    if (retryAfterSeconds != null) {
      response.setHeader("Retry-After", Long.toString(retryAfterSeconds));
    }
    if (bearerChallenge) {
      response.setHeader("WWW-Authenticate", "Bearer");
    }
    objectMapper.writeValue(response.getOutputStream(), problem);
  }
}
