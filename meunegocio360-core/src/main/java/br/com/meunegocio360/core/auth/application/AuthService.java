package br.com.meunegocio360.core.auth.application;

import br.com.meunegocio360.core.auth.domain.CredencialService;
import br.com.meunegocio360.core.auth.domain.RefreshToken;
import br.com.meunegocio360.core.auth.domain.RefreshTokenRepositorio;
import br.com.meunegocio360.core.auth.domain.TokenService;
import br.com.meunegocio360.core.auth.domain.Usuario;
import br.com.meunegocio360.core.auth.domain.UsuarioRepositorio;
import br.com.meunegocio360.core.shared.error.ErroAutenticacao;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
  private static final String MENSAGEM_SENHA_INVALIDA = "E-mail ou senha inválidos.";

  private final UsuarioRepositorio usuarioRepositorio;
  private final RefreshTokenRepositorio refreshTokenRepositorio;
  private final CredencialService credencialService;
  private final TokenService tokenService;
  private final Clock clock;
  private final int maxTentativas;
  private final int bloqueioMinutos;
  private final int accessTokenMinutes;
  private final int refreshTokenDays;
  private final String hashFicticio;

  public AuthService(
      UsuarioRepositorio usuarioRepositorio,
      RefreshTokenRepositorio refreshTokenRepositorio,
      CredencialService credencialService,
      TokenService tokenService,
      Clock clock,
      @Value("${app.auth.max-tentativas:5}") int maxTentativas,
      @Value("${app.auth.bloqueio-minutos:15}") int bloqueioMinutos,
      @Value("${app.auth.jwt.expiracao-minutos:15}") int accessTokenMinutes,
      @Value("${app.auth.refresh.expiracao-dias:7}") int refreshTokenDays) {
    this.usuarioRepositorio = usuarioRepositorio;
    this.refreshTokenRepositorio = refreshTokenRepositorio;
    this.credencialService = credencialService;
    this.tokenService = tokenService;
    this.clock = clock;
    this.maxTentativas = maxTentativas;
    this.bloqueioMinutos = bloqueioMinutos;
    this.accessTokenMinutes = accessTokenMinutes;
    this.refreshTokenDays = refreshTokenDays;
    this.hashFicticio = credencialService.codificar(UUID.randomUUID().toString());
  }

  @Transactional(noRollbackFor = ErroAutenticacao.class)
  public Sessao login(String email, String senha) {
    Instant agora = clock.instant();
    String emailNormalizado = normalizarEmail(email);
    Optional<Usuario> usuarioEncontrado =
        usuarioRepositorio.buscarPorEmailParaAtualizacao(emailNormalizado);

    if (usuarioEncontrado.isEmpty()) {
      credencialService.corresponde(senha, hashFicticio);
      LOGGER.warn("event=auth.login outcome=invalid_credentials");
      throw ErroAutenticacao.credenciaisInvalidas();
    }

    Usuario usuario = usuarioEncontrado.orElseThrow();
    usuario.limparBloqueioExpirado(agora);
    if (usuario.estaBloqueado(agora)) {
      long retryAfter = segundosRestantes(usuario.getBloqueadoAte(), agora);
      LOGGER.warn("event=auth.login outcome=temporarily_locked retryAfterSeconds={}", retryAfter);
      throw ErroAutenticacao.contaBloqueada(retryAfter);
    }

    boolean senhaCorreta = credencialService.corresponde(senha, usuario.getSenhaHash());
    if (!usuario.isAtivo() || !senhaCorreta) {
      if (usuario.isAtivo()) {
        usuario.registrarFalha(agora, maxTentativas, bloqueioMinutos);
        usuarioRepositorio.salvar(usuario);
        if (usuario.estaBloqueado(agora)) {
          refreshTokenRepositorio.revogarTodasDoUsuario(usuario.getId(), agora);
          LOGGER.warn("event=auth.account outcome=temporarily_locked");
        }
      }
      LOGGER.warn("event=auth.login outcome=invalid_credentials");
      throw new ErroAutenticacao(
          401, "INVALID_CREDENTIALS", "Não autenticado", MENSAGEM_SENHA_INVALIDA, null);
    }

    usuario.registrarLoginBemSucedido(agora);
    usuarioRepositorio.salvar(usuario);
    LOGGER.info("event=auth.login outcome=success perfil={}", usuario.getPerfil());
    return criarSessao(usuario, UUID.randomUUID(), agora);
  }

  @Transactional(noRollbackFor = ErroAutenticacao.class)
  public Sessao refresh(String valorRefresh) {
    Instant agora = clock.instant();
    if (valorRefresh == null || valorRefresh.isBlank()) {
      throw ErroAutenticacao.refreshInvalido();
    }

    Optional<RefreshToken> tokenEncontrado =
        refreshTokenRepositorio.buscarPorHashParaAtualizacao(
            tokenService.hashRefresh(valorRefresh));
    if (tokenEncontrado.isEmpty()) {
      throw ErroAutenticacao.refreshInvalido();
    }

    RefreshToken token = tokenEncontrado.orElseThrow();
    if (!token.estaValido(agora)) {
      if (token.getRevogadoEm() != null) {
        refreshTokenRepositorio.revogarFamilia(token.getFamiliaId(), agora);
      }
      throw ErroAutenticacao.refreshInvalido();
    }

    Optional<Usuario> usuarioEncontrado = usuarioRepositorio.buscarPorId(token.getUsuarioId());
    if (usuarioEncontrado.isEmpty()) {
      refreshTokenRepositorio.revogarFamilia(token.getFamiliaId(), agora);
      throw ErroAutenticacao.refreshInvalido();
    }

    Usuario usuario = usuarioEncontrado.orElseThrow();
    if (!usuario.isAtivo() || usuario.estaBloqueado(agora)) {
      refreshTokenRepositorio.revogarFamilia(token.getFamiliaId(), agora);
      throw ErroAutenticacao.refreshInvalido();
    }

    token.revogar(agora);
    refreshTokenRepositorio.salvar(token);
    return criarSessao(usuario, token.getFamiliaId(), agora);
  }

  @Transactional
  public void logout(String valorRefresh) {
    if (valorRefresh == null || valorRefresh.isBlank()) {
      return;
    }
    refreshTokenRepositorio
        .buscarPorHashParaAtualizacao(tokenService.hashRefresh(valorRefresh))
        .ifPresent(
            token -> refreshTokenRepositorio.revogarFamilia(token.getFamiliaId(), clock.instant()));
  }

  @Transactional(readOnly = true)
  public UsuarioResumo buscarUsuario(UUID id) {
    Usuario usuario =
        usuarioRepositorio.buscarPorId(id).orElseThrow(ErroAutenticacao::naoAutorizado);
    return new UsuarioResumo(
        usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil());
  }

  private Sessao criarSessao(Usuario usuario, UUID familiaId, Instant agora) {
    String refreshEmClaro = tokenService.novoRefresh();
    RefreshToken refreshToken =
        new RefreshToken(
            usuario.getId(),
            familiaId,
            tokenService.hashRefresh(refreshEmClaro),
            agora.plus(Duration.ofDays(refreshTokenDays)),
            agora);
    refreshTokenRepositorio.salvar(refreshToken);
    return new Sessao(
        tokenService.emitirAcesso(usuario, agora),
        accessTokenMinutes * 60,
        new UsuarioResumo(
            usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil()),
        refreshEmClaro,
        refreshTokenDays * 86400L);
  }

  private static String normalizarEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  private static long segundosRestantes(Instant bloqueadoAte, Instant agora) {
    return Math.max(1, (long) Math.ceil(Duration.between(agora, bloqueadoAte).toMillis() / 1000.0));
  }

  public record UsuarioResumo(UUID id, String nome, String email, String perfil) {}

  public record Sessao(
      String accessToken,
      int expiresIn,
      UsuarioResumo usuario,
      String refreshToken,
      long refreshMaxAgeSeconds) {

    @Override
    public String toString() {
      return "Sessao[accessToken=redacted, expiresIn=%d, refreshToken=redacted]"
          .formatted(expiresIn);
    }
  }
}
