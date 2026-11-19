package br.com.meunegocio360.core.auth.application;

import br.com.meunegocio360.core.auth.domain.AuthPolicy;
import br.com.meunegocio360.core.auth.domain.RefreshToken;
import br.com.meunegocio360.core.auth.domain.Usuario;
import br.com.meunegocio360.core.auth.domain.exception.AccountTemporarilyLockedException;
import br.com.meunegocio360.core.auth.domain.exception.InvalidCredentialsException;
import br.com.meunegocio360.core.auth.domain.port.AccessTokenIssuer;
import br.com.meunegocio360.core.auth.domain.port.AccessTokenIssuer.AccessToken;
import br.com.meunegocio360.core.auth.domain.port.PasswordHasher;
import br.com.meunegocio360.core.auth.domain.port.RefreshTokenCodec;
import br.com.meunegocio360.core.auth.domain.port.RefreshTokenRepository;
import br.com.meunegocio360.core.auth.domain.port.UsuarioRepository;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginUseCase {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoginUseCase.class);
  private static final int MAX_BCRYPT_BYTES = 72;

  private final UsuarioRepository usuarioRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordHasher passwordHasher;
  private final AccessTokenIssuer accessTokenIssuer;
  private final RefreshTokenCodec refreshTokenCodec;
  private final AuthPolicy policy;
  private final Clock clock;
  private final String dummyPasswordHash;

  public LoginUseCase(
      UsuarioRepository usuarioRepository,
      RefreshTokenRepository refreshTokenRepository,
      PasswordHasher passwordHasher,
      AccessTokenIssuer accessTokenIssuer,
      RefreshTokenCodec refreshTokenCodec,
      AuthPolicy policy,
      Clock clock) {
    this.usuarioRepository = usuarioRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.passwordHasher = passwordHasher;
    this.accessTokenIssuer = accessTokenIssuer;
    this.refreshTokenCodec = refreshTokenCodec;
    this.policy = policy;
    this.clock = clock;
    this.dummyPasswordHash = passwordHasher.hash(UUID.randomUUID().toString());
  }

  @Transactional(
      noRollbackFor = {InvalidCredentialsException.class, AccountTemporarilyLockedException.class})
  public AuthSession executar(String emailInformado, String senha) {
    String email = normalizarEmail(emailInformado);
    Optional<Usuario> encontrado = usuarioRepository.buscarPorEmailParaAtualizacao(email);
    if (encontrado.isEmpty()) {
      verificarHashFicticio(senha);
      registrarFalha();
      throw new InvalidCredentialsException();
    }

    Usuario usuario = encontrado.get();
    Instant agora = clock.instant();
    if (!usuario.ativo()) {
      passwordCorresponde(senha, usuario.senhaHash());
      registrarFalha();
      throw new InvalidCredentialsException();
    }
    if (usuario.estaBloqueado(agora)) {
      long retryAfter = usuario.bloqueadoAte().getEpochSecond() - agora.getEpochSecond();
      LOGGER.atWarn().addKeyValue("event", "auth.account_locked").log("Autenticação recusada");
      throw new AccountTemporarilyLockedException(retryAfter);
    }

    Usuario desbloqueado = usuario.limparBloqueioExpirado(agora);
    if (desbloqueado != usuario) {
      usuario = usuarioRepository.salvar(desbloqueado);
    }

    if (!passwordCorresponde(senha, usuario.senhaHash())) {
      registrarFalha(usuario, agora);
      throw new InvalidCredentialsException();
    }

    Usuario autenticado = usuarioRepository.salvar(usuario.registrarSucesso(agora));
    LOGGER.atInfo().addKeyValue("event", "auth.login_success").log("Autenticação concluída");
    return criarSessao(autenticado, agora, UUID.randomUUID());
  }

  private AuthSession criarSessao(Usuario usuario, Instant agora, UUID familiaId) {
    AccessToken accessToken = accessTokenIssuer.emitir(usuario, agora);
    String rawRefreshToken = refreshTokenCodec.gerar();
    refreshTokenRepository.salvar(
        new RefreshToken(
            UUID.randomUUID(),
            usuario.id(),
            familiaId,
            refreshTokenCodec.hash(rawRefreshToken),
            agora.plus(policy.duracaoRefreshToken()),
            null,
            agora));
    return new AuthSession(
        accessToken.valor(), accessToken.expiraEmSegundos(), rawRefreshToken, resumir(usuario));
  }

  private void verificarHashFicticio(String senha) {
    passwordCorresponde(senha, dummyPasswordHash);
  }

  private boolean passwordCorresponde(String senha, String hash) {
    if (bcryptLengthSupported(senha)) {
      return passwordHasher.corresponde(senha, hash);
    }
    passwordHasher.corresponde("dummy-password", dummyPasswordHash);
    return false;
  }

  private void registrarFalha(Usuario usuario, Instant agora) {
    Usuario usuarioComFalha = usuario.registrarFalha(agora, policy);
    usuarioRepository.salvar(usuarioComFalha);
    if (usuarioComFalha.estaBloqueado(agora)) {
      refreshTokenRepository.revogarTodosDoUsuario(usuario.id(), agora);
      LOGGER
          .atWarn()
          .addKeyValue("event", "auth.account_blocked")
          .log("Conta bloqueada após falhas consecutivas");
    }
    registrarFalha();
  }

  private static void registrarFalha() {
    LOGGER.atWarn().addKeyValue("event", "auth.login_failure").log("Falha de autenticação");
  }

  private static boolean bcryptLengthSupported(String senha) {
    return senha.getBytes(StandardCharsets.UTF_8).length <= MAX_BCRYPT_BYTES;
  }

  private static String normalizarEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  static AuthenticatedUser resumir(Usuario usuario) {
    return new AuthenticatedUser(usuario.id(), usuario.nome(), usuario.email(), usuario.perfil());
  }
}
