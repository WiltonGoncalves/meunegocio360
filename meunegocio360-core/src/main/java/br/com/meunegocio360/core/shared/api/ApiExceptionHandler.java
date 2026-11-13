package br.com.meunegocio360.core.shared.api;

import br.com.meunegocio360.core.auth.domain.exception.AccountTemporarilyLockedException;
import br.com.meunegocio360.core.auth.domain.exception.InvalidCredentialsException;
import br.com.meunegocio360.core.auth.domain.exception.InvalidRefreshTokenException;
import br.com.meunegocio360.core.auth.domain.exception.UserNotAuthenticatedException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

  private final ApiProblemFactory problemFactory;

  public ApiExceptionHandler(ApiProblemFactory problemFactory) {
    this.problemFactory = problemFactory;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> validation(
      MethodArgumentNotValidException exception, WebRequest request) {
    List<ApiProblemFactory.ValidationFieldError> errors =
        exception.getBindingResult().getFieldErrors().stream()
            .map(
                error ->
                    new ApiProblemFactory.ValidationFieldError(
                        error.getField(),
                        error.getDefaultMessage() == null
                            ? "valor inválido"
                            : error.getDefaultMessage()))
            .toList();
    return response(
        HttpStatus.BAD_REQUEST,
        "VALIDATION_ERROR",
        "Dados inválidos",
        "Um ou mais campos são inválidos.",
        request,
        errors);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ProblemDetail> malformedJson(
      HttpMessageNotReadableException exception, WebRequest request) {
    return response(
        HttpStatus.BAD_REQUEST,
        "VALIDATION_ERROR",
        "Dados inválidos",
        "Corpo da requisição ausente ou malformado.",
        request);
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ProblemDetail> invalidCredentials(
      InvalidCredentialsException exception, WebRequest request) {
    return response(
        HttpStatus.UNAUTHORIZED,
        "INVALID_CREDENTIALS",
        "Não autenticado",
        "E-mail ou senha inválidos.",
        request);
  }

  @ExceptionHandler(InvalidRefreshTokenException.class)
  public ResponseEntity<ProblemDetail> invalidRefresh(
      InvalidRefreshTokenException exception, WebRequest request) {
    return response(
        HttpStatus.UNAUTHORIZED,
        "INVALID_REFRESH_TOKEN",
        "Sessão inválida",
        "Refresh token inválido ou expirado.",
        request);
  }

  @ExceptionHandler(UserNotAuthenticatedException.class)
  public ResponseEntity<ProblemDetail> unauthenticated(
      UserNotAuthenticatedException exception, WebRequest request) {
    return unauthorized(request);
  }

  @ExceptionHandler(AccountTemporarilyLockedException.class)
  public ResponseEntity<ProblemDetail> accountLocked(
      AccountTemporarilyLockedException exception, WebRequest request) {
    return response(
        HttpStatus.TOO_MANY_REQUESTS,
        "ACCOUNT_TEMPORARILY_LOCKED",
        "Conta temporariamente bloqueada",
        "Tente novamente após o período de bloqueio.",
        request,
        List.of(),
        exception.retryAfterSeconds());
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ProblemDetail> forbidden(
      AccessDeniedException exception, WebRequest request) {
    return response(
        HttpStatus.FORBIDDEN,
        "FORBIDDEN",
        "Acesso negado",
        "Você não tem permissão para acessar este recurso.",
        request);
  }

  @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
  public ResponseEntity<ProblemDetail> notFound(Exception exception, WebRequest request) {
    return response(
        HttpStatus.NOT_FOUND,
        "NOT_FOUND",
        "Recurso não encontrado",
        "O recurso solicitado não foi encontrado.",
        request);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> internalError(Exception exception, WebRequest request) {
    String traceId =
        (String) request.getAttribute(RequestIdFilter.TRACE_ID_ATTRIBUTE, WebRequest.SCOPE_REQUEST);
    LOGGER.error(
        "event=api_internal_error traceId={} exceptionType={}",
        traceId,
        exception.getClass().getSimpleName());
    return response(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "INTERNAL_ERROR",
        "Erro interno",
        "Ocorreu um erro inesperado.",
        request);
  }

  private ResponseEntity<ProblemDetail> unauthorized(WebRequest request) {
    return response(
        HttpStatus.UNAUTHORIZED,
        "UNAUTHORIZED",
        "Não autenticado",
        "Autenticação necessária ou token inválido.",
        request,
        List.of(),
        null,
        true);
  }

  private ResponseEntity<ProblemDetail> response(
      HttpStatus status, String code, String title, String detail, WebRequest request) {
    return response(status, code, title, detail, request, List.of());
  }

  private ResponseEntity<ProblemDetail> response(
      HttpStatus status,
      String code,
      String title,
      String detail,
      WebRequest request,
      List<ApiProblemFactory.ValidationFieldError> errors) {
    return response(status, code, title, detail, request, errors, null, false);
  }

  private ResponseEntity<ProblemDetail> response(
      HttpStatus status,
      String code,
      String title,
      String detail,
      WebRequest request,
      List<ApiProblemFactory.ValidationFieldError> errors,
      Long retryAfter) {
    return response(status, code, title, detail, request, errors, retryAfter, false);
  }

  private ResponseEntity<ProblemDetail> response(
      HttpStatus status,
      String code,
      String title,
      String detail,
      WebRequest request,
      List<ApiProblemFactory.ValidationFieldError> errors,
      Long retryAfter,
      boolean authenticate) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
    if (retryAfter != null) {
      headers.set(HttpHeaders.RETRY_AFTER, retryAfter.toString());
    }
    if (authenticate) {
      headers.set(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
    }
    ProblemDetail problem = problemFactory.create(status, code, title, detail, request, errors);
    return new ResponseEntity<>(problem, headers, status);
  }
}
