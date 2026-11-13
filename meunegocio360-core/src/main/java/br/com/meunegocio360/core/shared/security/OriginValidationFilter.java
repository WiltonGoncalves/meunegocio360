package br.com.meunegocio360.core.shared.security;

import br.com.meunegocio360.core.shared.error.ErroAutenticacao;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class OriginValidationFilter extends OncePerRequestFilter {

  private final Set<String> allowedOrigins;
  private final ProblemWriter problemWriter = new ProblemWriter();

  public OriginValidationFilter(@Value("${app.auth.cors.allowed-origins:}") String origins) {
    this.allowedOrigins =
        Arrays.stream(origins.split(","))
            .map(String::trim)
            .filter(origin -> !origin.isEmpty())
            .collect(Collectors.toUnmodifiableSet());
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String path = request.getRequestURI();
    String origin = request.getHeader("Origin");
    boolean usesRefreshCookie =
        path.equals("/api/v1/auth/refresh") || path.equals("/api/v1/auth/logout");
    if (usesRefreshCookie && origin != null && !allowedOrigins.contains(origin)) {
      ErroAutenticacao erro = ErroAutenticacao.origemNaoPermitida();
      problemWriter.write(
          request,
          response,
          erro.getStatus(),
          erro.getCode(),
          erro.getTitle(),
          erro.getMessage(),
          null,
          false);
      return;
    }
    filterChain.doFilter(request, response);
  }
}
