package br.com.meunegocio360.core.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

@Component
public class AuthRuntimeValidation implements ApplicationRunner {

  private final Environment environment;
  private final boolean cookieSecure;
  private final int maxTentativas;
  private final int bloqueioMinutos;
  private final int expiracaoJwtMinutos;
  private final int expiracaoRefreshDias;

  public AuthRuntimeValidation(
      Environment environment,
      @Value("${app.auth.cookie.secure:true}") boolean cookieSecure,
      @Value("${app.auth.max-tentativas:5}") int maxTentativas,
      @Value("${app.auth.bloqueio-minutos:15}") int bloqueioMinutos,
      @Value("${app.auth.jwt.expiracao-minutos:15}") int expiracaoJwtMinutos,
      @Value("${app.auth.refresh.expiracao-dias:7}") int expiracaoRefreshDias) {
    this.environment = environment;
    this.cookieSecure = cookieSecure;
    this.maxTentativas = maxTentativas;
    this.bloqueioMinutos = bloqueioMinutos;
    this.expiracaoJwtMinutos = expiracaoJwtMinutos;
    this.expiracaoRefreshDias = expiracaoRefreshDias;
  }

  @Override
  public void run(ApplicationArguments arguments) {
    if (maxTentativas < 1
        || bloqueioMinutos < 1
        || expiracaoJwtMinutos < 1
        || expiracaoRefreshDias < 1) {
      throw new IllegalStateException("As configurações de autenticação devem ser positivas.");
    }
    if (!cookieSecure && !environment.acceptsProfiles(Profiles.of("dev"))) {
      throw new IllegalStateException(
          "APP_AUTH_COOKIE_SECURE=false só pode ser usado no perfil dev.");
    }
  }
}
