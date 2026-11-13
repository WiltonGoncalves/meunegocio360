package br.com.meunegocio360.core.shared.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

public class ProblemAccessDeniedHandler implements AccessDeniedHandler {

  @Override
  public void handle(
      HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception)
      throws IOException, ServletException {
    new ProblemWriter()
        .write(
            request,
            response,
            403,
            "FORBIDDEN",
            "Acesso negado",
            "Você não tem permissão para acessar este recurso.",
            null,
            false);
  }
}
