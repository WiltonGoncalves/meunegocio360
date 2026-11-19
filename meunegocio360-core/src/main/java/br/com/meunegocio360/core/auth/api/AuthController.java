package br.com.meunegocio360.core.auth.api;

import static org.springframework.http.HttpHeaders.SET_COOKIE;

import br.com.meunegocio360.core.auth.application.AuthSession;
import br.com.meunegocio360.core.auth.application.AuthenticatedUser;
import br.com.meunegocio360.core.auth.application.GetCurrentUserUseCase;
import br.com.meunegocio360.core.auth.application.LoginUseCase;
import br.com.meunegocio360.core.auth.application.LogoutUseCase;
import br.com.meunegocio360.core.auth.application.RefreshUseCase;
import br.com.meunegocio360.core.auth.domain.exception.InvalidRefreshTokenException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação", description = "Login e manutenção da sessão do usuário")
public class AuthController {

  private final LoginUseCase loginUseCase;
  private final RefreshUseCase refreshUseCase;
  private final LogoutUseCase logoutUseCase;
  private final GetCurrentUserUseCase getCurrentUserUseCase;
  private final AuthCookieFactory cookieFactory;

  public AuthController(
      LoginUseCase loginUseCase,
      RefreshUseCase refreshUseCase,
      LogoutUseCase logoutUseCase,
      GetCurrentUserUseCase getCurrentUserUseCase,
      AuthCookieFactory cookieFactory) {
    this.loginUseCase = loginUseCase;
    this.refreshUseCase = refreshUseCase;
    this.logoutUseCase = logoutUseCase;
    this.getCurrentUserUseCase = getCurrentUserUseCase;
    this.cookieFactory = cookieFactory;
  }

  @PostMapping("/login")
  @Operation(summary = "Autentica usuário e inicia uma sessão")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Sessão iniciada; refresh enviado somente por cookie"),
    @ApiResponse(
        responseCode = "400",
        description = "Dados inválidos",
        content =
            @Content(
                mediaType = "application/problem+json",
                examples =
                    @ExampleObject(value = "{\"code\":\"VALIDATION_ERROR\",\"status\":400}"))),
    @ApiResponse(
        responseCode = "401",
        description = "Credenciais inválidas",
        content =
            @Content(
                mediaType = "application/problem+json",
                examples =
                    @ExampleObject(value = "{\"code\":\"INVALID_CREDENTIALS\",\"status\":401}"))),
    @ApiResponse(
        responseCode = "429",
        description = "Conta temporariamente bloqueada",
        content =
            @Content(
                mediaType = "application/problem+json",
                examples =
                    @ExampleObject(
                        value = "{\"code\":\"ACCOUNT_TEMPORARILY_LOCKED\",\"status\":429}")))
  })
  public AuthResponse login(
      @Valid @RequestBody LoginRequest request, HttpServletResponse response) {
    AuthSession session = loginUseCase.executar(request.email(), request.senha());
    response.addHeader(SET_COOKIE, cookieFactory.criar(session.rawRefreshToken()).toString());
    return toResponse(session);
  }

  @PostMapping("/refresh")
  @Operation(summary = "Rotaciona o refresh token e renova a sessão")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Sessão renovada com novo cookie"),
    @ApiResponse(
        responseCode = "401",
        description = "Refresh inválido; cookie removido",
        content =
            @Content(
                mediaType = "application/problem+json",
                examples =
                    @ExampleObject(value = "{\"code\":\"INVALID_REFRESH_TOKEN\",\"status\":401}"))),
    @ApiResponse(
        responseCode = "403",
        description = "Origem não permitida",
        content =
            @Content(
                mediaType = "application/problem+json",
                examples =
                    @ExampleObject(value = "{\"code\":\"ORIGIN_NOT_ALLOWED\",\"status\":403}")))
  })
  public AuthResponse refresh(
      @CookieValue(name = AuthCookieFactory.COOKIE_NAME, required = false) String refreshToken,
      HttpServletResponse response) {
    try {
      AuthSession session = refreshUseCase.executar(refreshToken);
      response.addHeader(SET_COOKIE, cookieFactory.criar(session.rawRefreshToken()).toString());
      return toResponse(session);
    } catch (InvalidRefreshTokenException exception) {
      response.addHeader(SET_COOKIE, cookieFactory.limpar().toString());
      throw exception;
    }
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Revoga a família da sessão e remove o cookie")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Sessão encerrada (idempotente)"),
    @ApiResponse(
        responseCode = "403",
        description = "Origem não permitida",
        content =
            @Content(
                mediaType = "application/problem+json",
                examples =
                    @ExampleObject(value = "{\"code\":\"ORIGIN_NOT_ALLOWED\",\"status\":403}")))
  })
  public void logout(
      @CookieValue(name = AuthCookieFactory.COOKIE_NAME, required = false) String refreshToken,
      HttpServletResponse response) {
    logoutUseCase.executar(refreshToken);
    response.addHeader(SET_COOKIE, cookieFactory.limpar().toString());
  }

  @GetMapping("/me")
  @Operation(summary = "Retorna o usuário autenticado")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Usuário autenticado"),
    @ApiResponse(
        responseCode = "401",
        description = "Token ausente, inválido ou expirado",
        content =
            @Content(
                mediaType = "application/problem+json",
                examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"status\":401}")))
  })
  public AuthUserResponse me(@AuthenticationPrincipal Jwt jwt) {
    return toResponse(getCurrentUserUseCase.executar(jwt.getSubject()));
  }

  private static AuthResponse toResponse(AuthSession session) {
    return new AuthResponse(
        "Bearer", session.accessToken(), session.expiresIn(), toResponse(session.usuario()));
  }

  private static AuthUserResponse toResponse(AuthenticatedUser usuario) {
    return new AuthUserResponse(
        usuario.id(), usuario.nome(), usuario.email(), usuario.perfil().name());
  }
}
