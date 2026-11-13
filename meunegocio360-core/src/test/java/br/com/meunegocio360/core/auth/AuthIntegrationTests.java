package br.com.meunegocio360.core.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.meunegocio360.core.auth.domain.Perfil;
import br.com.meunegocio360.core.auth.domain.Usuario;
import br.com.meunegocio360.core.auth.domain.port.PasswordHasher;
import br.com.meunegocio360.core.auth.domain.port.RefreshTokenCodec;
import br.com.meunegocio360.core.auth.domain.port.UsuarioRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class AuthIntegrationTests {

  private static final String PASSWORD = "SenhaSegura123";
  private static final String JWT_SECRET = newJwtSecret();

  @Container
  private static final MySQLContainer<?> MYSQL =
      new MySQLContainer<>("mysql:8.4")
          .withDatabaseName("meunegocio360_test")
          .withUsername("auth_test")
          .withPassword("auth_test_password")
          .withEnv("TZ", "UTC");

  @Autowired private MockMvc mockMvc;

  @Autowired private JsonMapper jsonMapper;

  @Autowired private JwtDecoder jwtDecoder;

  @Autowired private UsuarioRepository usuarioRepository;

  @Autowired private PasswordHasher passwordHasher;

  @Autowired private RefreshTokenCodec refreshTokenCodec;

  @Autowired private JdbcTemplate jdbcTemplate;

  private UUID usuarioId;
  private String email;

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("DB_URL", MYSQL::getJdbcUrl);
    registry.add("DB_USER", MYSQL::getUsername);
    registry.add("DB_PASSWORD", MYSQL::getPassword);
    registry.add("JWT_SECRET", () -> JWT_SECRET);
    registry.add("CORS_ALLOWED_ORIGINS", () -> "http://localhost:5173");
    registry.add("APP_AUTH_COOKIE_SECURE", () -> "true");
  }

  @BeforeEach
  void createActiveUser() {
    usuarioId = UUID.randomUUID();
    email = "teste-" + usuarioId + "@example.com";
    Instant now = Clock.systemUTC().instant();
    usuarioRepository.salvar(
        new Usuario(
            usuarioId,
            "Usuário de Teste",
            email,
            passwordHasher.hash(PASSWORD),
            Perfil.USUARIO,
            true,
            0,
            null,
            null,
            now,
            now,
            null));
  }

  @Test
  void loginIssuesShortLivedJwtAndHashedRefreshCookie() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody(email, PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresIn").value(900))
            .andExpect(jsonPath("$.usuario.id").value(usuarioId.toString()))
            .andExpect(jsonPath("$.usuario.nome").value("Usuário de Teste"))
            .andExpect(jsonPath("$.usuario.perfil").value("USUARIO"))
            .andExpect(
                header()
                    .string(
                        "X-Request-Id",
                        org.hamcrest.Matchers.not(org.hamcrest.Matchers.isEmptyOrNullString())))
            .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    String refreshToken = cookieValue(result);
    assertFalse(responseBody.contains(refreshToken));
    assertTrue(result.getResponse().getHeader("Set-Cookie").contains("HttpOnly"));
    assertTrue(result.getResponse().getHeader("Set-Cookie").contains("SameSite=Strict"));
    assertTrue(result.getResponse().getHeader("Set-Cookie").contains("Path=/api/v1/auth"));
    assertTrue(result.getResponse().getHeader("Set-Cookie").contains("Secure"));

    Jwt jwt = jwtDecoder.decode(jsonMapper.readTree(responseBody).get("accessToken").asText());
    assertEquals("meunegocio360-core", jwt.getIssuer().toString());
    assertEquals(usuarioId.toString(), jwt.getSubject());
    assertNotNull(jwt.getId());
    assertEquals("USUARIO", jwt.getClaimAsString("perfil"));
    assertEquals(900, jwt.getExpiresAt().getEpochSecond() - jwt.getIssuedAt().getEpochSecond());
    assertFalse(jwt.getClaims().containsKey("email"));

    String hash = refreshTokenCodec.hash(refreshToken);
    assertEquals(1, countRefreshTokenByHash(hash));
    assertEquals(0, countRefreshTokenByHash(refreshToken));
    assertNotNull(usuarioRepository.buscarPorId(usuarioId).orElseThrow().ultimoLoginEm());
  }

  @Test
  void unknownEmailAndWrongPasswordHaveSameGenericResponse() throws Exception {
    MvcResult wrongPassword =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody(email, "SenhaErrada123")))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andReturn();

    MvcResult unknownEmail =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody("inexistente@example.com", PASSWORD)))
            .andExpect(status().isUnauthorized())
            .andReturn();

    JsonNode wrong = jsonMapper.readTree(wrongPassword.getResponse().getContentAsString());
    JsonNode unknown = jsonMapper.readTree(unknownEmail.getResponse().getContentAsString());
    assertEquals(wrong.get("code"), unknown.get("code"));
    assertEquals("INVALID_CREDENTIALS", wrong.get("code").asText());
    assertEquals(wrong.get("detail"), unknown.get("detail"));
    assertEquals("E-mail ou senha inválidos.", wrong.get("detail").asText());
    assertNull(wrongPassword.getResponse().getHeader("Set-Cookie"));
  }

  @Test
  void locksAccountAfterFiveFailuresAndReturnsRetryAfter() throws Exception {
    String refreshToken = cookieValue(login());

    for (int failure = 0; failure < 5; failure++) {
      mockMvc
          .perform(
              post("/api/v1/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(loginBody(email, "SenhaErrada123")))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(email, PASSWORD)))
        .andExpect(status().isTooManyRequests())
        .andExpect(jsonPath("$.code").value("ACCOUNT_TEMPORARILY_LOCKED"))
        .andExpect(header().exists("Retry-After"));

    Usuario atualizado = usuarioRepository.buscarPorId(usuarioId).orElseThrow();
    assertEquals(5, atualizado.tentativasFalhas());
    assertNotNull(atualizado.bloqueadoAte());

    mockMvc
        .perform(
            post("/api/v1/auth/refresh")
                .cookie(new jakarta.servlet.http.Cookie("mn360_refresh", refreshToken)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
  }

  @Test
  void rotatesRefreshTokenDetectsReuseAndRevokesFamily() throws Exception {
    MvcResult login = login();
    String originalRefresh = cookieValue(login);
    Instant lastLogin = usuarioRepository.buscarPorId(usuarioId).orElseThrow().ultimoLoginEm();

    MvcResult refresh =
        mockMvc
            .perform(
                post("/api/v1/auth/refresh")
                    .cookie(new jakarta.servlet.http.Cookie("mn360_refresh", originalRefresh)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.usuario.id").value(usuarioId.toString()))
            .andReturn();
    String rotatedRefresh = cookieValue(refresh);
    assertNotEquals(originalRefresh, rotatedRefresh);
    assertEquals(lastLogin, usuarioRepository.buscarPorId(usuarioId).orElseThrow().ultimoLoginEm());
    assertEquals(0, countRefreshTokenByHash(originalRefresh));
    assertEquals(1, countRefreshTokenByHash(refreshTokenCodec.hash(rotatedRefresh)));

    mockMvc
        .perform(
            post("/api/v1/auth/refresh")
                .cookie(new jakarta.servlet.http.Cookie("mn360_refresh", originalRefresh)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));

    mockMvc
        .perform(
            post("/api/v1/auth/refresh")
                .cookie(new jakarta.servlet.http.Cookie("mn360_refresh", rotatedRefresh)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
  }

  @Test
  void logoutIsIdempotentAndClearsCookie() throws Exception {
    String refreshToken = cookieValue(login());

    mockMvc
        .perform(
            post("/api/v1/auth/logout")
                .cookie(new jakarta.servlet.http.Cookie("mn360_refresh", refreshToken)))
        .andExpect(status().isNoContent())
        .andExpect(
            header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")));

    mockMvc
        .perform(post("/api/v1/auth/logout"))
        .andExpect(status().isNoContent())
        .andExpect(
            header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")));

    mockMvc
        .perform(
            post("/api/v1/auth/refresh")
                .cookie(new jakarta.servlet.http.Cookie("mn360_refresh", refreshToken)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void rejectsDisallowedOriginWithoutRotatingRefreshCookie() throws Exception {
    String refreshToken = cookieValue(login());
    mockMvc
        .perform(
            post("/api/v1/auth/refresh")
                .cookie(new jakarta.servlet.http.Cookie("mn360_refresh", refreshToken))
                .header("Origin", "https://malicioso.example"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ORIGIN_NOT_ALLOWED"));

    mockMvc
        .perform(
            post("/api/v1/auth/refresh")
                .cookie(new jakarta.servlet.http.Cookie("mn360_refresh", refreshToken)))
        .andExpect(status().isOk());
  }

  @Test
  void returnsValidationAndUnauthorizedProblemDetails() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"invalido\",\"senha\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.traceId").isNotEmpty())
        .andExpect(jsonPath("$.errors").isArray());

    mockMvc
        .perform(get("/api/v1/auth/me"))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(header().string("WWW-Authenticate", "Bearer"))
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

    mockMvc
        .perform(get("/api/v1/rota-inexistente"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
  }

  private MvcResult login() throws Exception {
    return mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(email, PASSWORD)))
        .andExpect(status().isOk())
        .andReturn();
  }

  private int countRefreshTokenByHash(String tokenHash) {
    return jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM refresh_token WHERE token_hash = ?", Integer.class, tokenHash);
  }

  private static String loginBody(String email, String password) {
    return "{\"email\":\"" + email + "\",\"senha\":\"" + password + "\"}";
  }

  private static String cookieValue(MvcResult result) {
    String cookie = result.getResponse().getHeader("Set-Cookie");
    assertNotNull(cookie);
    return cookie.substring("mn360_refresh=".length(), cookie.indexOf(';'));
  }

  private static String newJwtSecret() {
    byte[] secret = new byte[48];
    new java.security.SecureRandom().nextBytes(secret);
    return Base64.getEncoder().encodeToString(secret);
  }
}
