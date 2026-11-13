package br.com.meunegocio360.core.auth.api;

import br.com.meunegocio360.core.auth.application.AuthService;
import br.com.meunegocio360.core.auth.application.AuthService.Sessao;
import br.com.meunegocio360.core.auth.application.AuthService.UsuarioResumo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(
    name = "Autenticação",
    description = "Login, renovação de sessão e consulta do usuário autenticado.")
public class AuthController {

  private final AuthService authService;
  private final RefreshCookie refreshCookie;

  public AuthController(AuthService authService, RefreshCookie refreshCookie) {
    this.authService = authService;
    this.refreshCookie = refreshCookie;
  }

  @PostMapping("/login")
  @Operation(summary = "Autenticar usuário com e-mail e senha.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Credenciais válidas.",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"tokenType\":\"Bearer\",\"accessToken\":\"<jwt>\",\"expiresIn\":900,\"usuario\":{\"id\":\"0b6c6a8e-3a1f-4a4b-9a64-2d0f1c3f6b11\",\"nome\":\"Administrador\",\"email\":\"admin@meunegocio360.com.br\",\"perfil\":\"ADMIN\"}}"))),
    @ApiResponse(
        responseCode = "400",
        description = "Entrada inválida.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema =
                    @Schema(
                        implementation =
                            br.com.meunegocio360.core.shared.error.ProblemResponse.class),
                examples =
                    @ExampleObject(value = "{\"code\":\"VALIDATION_ERROR\",\"status\":400}"))),
    @ApiResponse(
        responseCode = "401",
        description = "Credenciais inválidas.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema =
                    @Schema(
                        implementation =
                            br.com.meunegocio360.core.shared.error.ProblemResponse.class),
                examples =
                    @ExampleObject(value = "{\"code\":\"INVALID_CREDENTIALS\",\"status\":401}"))),
    @ApiResponse(
        responseCode = "429",
        description = "Conta temporariamente bloqueada.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema =
                    @Schema(
                        implementation =
                            br.com.meunegocio360.core.shared.error.ProblemResponse.class),
                examples =
                    @ExampleObject(
                        value = "{\"code\":\"ACCOUNT_TEMPORARILY_LOCKED\",\"status\":429}")))
  })
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    Sessao sessao = authService.login(request.email(), request.senha());
    return ResponseEntity.ok()
        .header(
            HttpHeaders.SET_COOKIE,
            refreshCookie.set(sessao.refreshToken(), sessao.refreshMaxAgeSeconds()))
        .body(resposta(sessao));
  }

  @PostMapping("/refresh")
  @Operation(summary = "Rotacionar o refresh token e emitir novo token de acesso.")
  @Parameter(
      name = RefreshCookie.NAME,
      in = ParameterIn.COOKIE,
      required = false,
      description = "Cookie HttpOnly emitido no login.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Sessão renovada.",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"tokenType\":\"Bearer\",\"accessToken\":\"<jwt>\",\"expiresIn\":900,\"usuario\":{\"id\":\"0b6c6a8e-3a1f-4a4b-9a64-2d0f1c3f6b11\",\"nome\":\"Administrador\",\"email\":\"admin@meunegocio360.com.br\",\"perfil\":\"ADMIN\"}}"))),
    @ApiResponse(
        responseCode = "401",
        description = "Cookie ausente, expirado, inválido ou reutilizado.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema =
                    @Schema(
                        implementation =
                            br.com.meunegocio360.core.shared.error.ProblemResponse.class),
                examples =
                    @ExampleObject(value = "{\"code\":\"INVALID_REFRESH_TOKEN\",\"status\":401}"))),
    @ApiResponse(
        responseCode = "403",
        description = "Origem não permitida.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema =
                    @Schema(
                        implementation =
                            br.com.meunegocio360.core.shared.error.ProblemResponse.class),
                examples =
                    @ExampleObject(value = "{\"code\":\"ORIGIN_NOT_ALLOWED\",\"status\":403}")))
  })
  public ResponseEntity<LoginResponse> refresh(
      @CookieValue(name = RefreshCookie.NAME, required = false) String refreshToken) {
    Sessao sessao = authService.refresh(refreshToken);
    return ResponseEntity.ok()
        .header(
            HttpHeaders.SET_COOKIE,
            refreshCookie.set(sessao.refreshToken(), sessao.refreshMaxAgeSeconds()))
        .body(resposta(sessao));
  }

  @PostMapping("/logout")
  @Operation(summary = "Encerrar a sessão e revogar a família de refresh tokens.")
  @Parameter(
      name = RefreshCookie.NAME,
      in = ParameterIn.COOKIE,
      description = "Cookie HttpOnly da sessão.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Cookie expirado e sessão revogada."),
    @ApiResponse(
        responseCode = "403",
        description = "Origem não permitida.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema =
                    @Schema(
                        implementation =
                            br.com.meunegocio360.core.shared.error.ProblemResponse.class),
                examples =
                    @ExampleObject(value = "{\"code\":\"ORIGIN_NOT_ALLOWED\",\"status\":403}")))
  })
  public ResponseEntity<Void> logout(
      @CookieValue(name = RefreshCookie.NAME, required = false) String refreshToken) {
    authService.logout(refreshToken);
    return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, refreshCookie.clear()).build();
  }

  @GetMapping("/me")
  @Operation(
      summary = "Consultar o usuário autenticado.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Dados do usuário autenticado."),
    @ApiResponse(
        responseCode = "401",
        description = "Token de acesso ausente, inválido ou expirado.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema =
                    @Schema(
                        implementation =
                            br.com.meunegocio360.core.shared.error.ProblemResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"status\":401}")))
  })
  public UsuarioResponse me(@AuthenticationPrincipal Jwt jwt) {
    UsuarioResumo usuario = authService.buscarUsuario(UUID.fromString(jwt.getSubject()));
    return resposta(usuario);
  }

  private static LoginResponse resposta(Sessao sessao) {
    return new LoginResponse(
        "Bearer", sessao.accessToken(), sessao.expiresIn(), resposta(sessao.usuario()));
  }

  private static UsuarioResponse resposta(UsuarioResumo usuario) {
    return new UsuarioResponse(usuario.id(), usuario.nome(), usuario.email(), usuario.perfil());
  }
}
