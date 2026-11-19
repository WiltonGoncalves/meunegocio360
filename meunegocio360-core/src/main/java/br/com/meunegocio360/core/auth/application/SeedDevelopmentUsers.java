package br.com.meunegocio360.core.auth.application;

import br.com.meunegocio360.core.auth.domain.Perfil;
import br.com.meunegocio360.core.auth.domain.Usuario;
import br.com.meunegocio360.core.auth.domain.port.PasswordHasher;
import br.com.meunegocio360.core.auth.domain.port.UsuarioRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
public class SeedDevelopmentUsers implements ApplicationRunner {

  private final UsuarioRepository usuarioRepository;
  private final PasswordHasher passwordHasher;
  private final Clock clock;
  private final String adminPassword;
  private final String userPassword;

  public SeedDevelopmentUsers(
      UsuarioRepository usuarioRepository,
      PasswordHasher passwordHasher,
      Clock clock,
      @Value("${SEED_ADMIN_SENHA}") String adminPassword,
      @Value("${SEED_USUARIO_SENHA}") String userPassword) {
    this.usuarioRepository = usuarioRepository;
    this.passwordHasher = passwordHasher;
    this.clock = clock;
    this.adminPassword = adminPassword;
    this.userPassword = userPassword;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    criarSeAusente("Administrador", "admin@meunegocio360.com.br", adminPassword, Perfil.ADMIN);
    criarSeAusente("Usuário", "usuario@meunegocio360.com.br", userPassword, Perfil.USUARIO);
  }

  private void criarSeAusente(String nome, String email, String senha, Perfil perfil) {
    String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
    if (senha.length() < 8 || senha.length() > 72) {
      throw new IllegalArgumentException(
          "Development seed passwords must contain 8 to 72 characters.");
    }
    if (usuarioRepository.buscarPorEmail(emailNormalizado).isPresent()) {
      return;
    }
    Instant agora = clock.instant();
    usuarioRepository.salvar(
        new Usuario(
            UUID.randomUUID(),
            nome,
            emailNormalizado,
            passwordHasher.hash(senha),
            perfil,
            true,
            0,
            null,
            null,
            agora,
            agora,
            null));
  }
}
