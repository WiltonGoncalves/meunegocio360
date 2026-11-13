package br.com.meunegocio360.core.shared.error;

import br.com.meunegocio360.core.auth.api.RefreshCookie;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProblemAdvice {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProblemAdvice.class);
  private final RefreshCookie refreshCookie;

  public ProblemAdvice(RefreshCookie refreshCookie) {
    this.refreshCookie = refreshCookie;
  }

  @ExceptionHandler(ErroAutenticacao.class)
  public ResponseEntity<ProblemResponse> tratarAutenticacao(
      ErroAutenticacao erro, HttpServletRequest request) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("application/problem+json"));
    if (erro.getRetryAfterSeconds() != null) {
      headers.set(HttpHeaders.RETRY_AFTER, Long.toString(erro.getRetryAfterSeconds()));
    }
    if (erro.getCode().equals("UNAUTHORIZED")) {
      headers.set(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
    }
    if (request.getRequestURI().equals("/api/v1/auth/refresh")
        && erro.getStatus() == 401
        && erro.getCode().equals("INVALID_REFRESH_TOKEN")) {
      RefreshCookie.adicionarCookie(headers, refreshCookie.clear());
    }
    return new ResponseEntity<>(
        criarProblema(
            request,
            erro.getStatus(),
            erro.getCode(),
            erro.getTitle(),
            erro.getMessage(),
            List.of()),
        headers,
        HttpStatus.valueOf(erro.getStatus()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemResponse> tratarValidacao(
      MethodArgumentNotValidException erro, HttpServletRequest request) {
    List<ProblemResponse.FieldValidationError> errors =
        erro.getBindingResult().getFieldErrors().stream().map(ProblemAdvice::mapearErro).toList();
    return resposta(
        request,
        400,
        "VALIDATION_ERROR",
        "Dados inválidos",
        "Um ou mais campos são inválidos.",
        errors);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ProblemResponse> tratarJsonInvalido(
      HttpMessageNotReadableException erro, HttpServletRequest request) {
    return resposta(
        request,
        400,
        "VALIDATION_ERROR",
        "Dados inválidos",
        "Um ou mais campos são inválidos.",
        List.of(new ProblemResponse.FieldValidationError("body", "deve conter um JSON válido")));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemResponse> tratarErroInesperado(
      Exception erro, HttpServletRequest request) {
    LOGGER.error(
        "Erro inesperado na requisição; traceId={}, exceptionType={}",
        MDC.get("requestId"),
        erro.getClass().getName());
    return resposta(
        request, 500, "INTERNAL_ERROR", "Erro interno", "Ocorreu um erro inesperado.", List.of());
  }

  private static ProblemResponse.FieldValidationError mapearErro(FieldError erro) {
    return new ProblemResponse.FieldValidationError(erro.getField(), erro.getDefaultMessage());
  }

  private static ResponseEntity<ProblemResponse> resposta(
      HttpServletRequest request,
      int status,
      String code,
      String title,
      String detail,
      List<ProblemResponse.FieldValidationError> errors) {
    return ResponseEntity.status(status)
        .contentType(MediaType.valueOf("application/problem+json"))
        .body(criarProblema(request, status, code, title, detail, errors));
  }

  private static ProblemResponse criarProblema(
      HttpServletRequest request,
      int status,
      String code,
      String title,
      String detail,
      List<ProblemResponse.FieldValidationError> errors) {
    String traceId = MDC.get("requestId");
    if (traceId == null) {
      traceId = UUID.randomUUID().toString();
    }
    return new ProblemResponse(
        URI.create("https://meunegocio360.com.br/problemas/" + code.toLowerCase()),
        title,
        status,
        detail,
        request.getRequestURI(),
        code,
        traceId,
        errors);
  }
}
