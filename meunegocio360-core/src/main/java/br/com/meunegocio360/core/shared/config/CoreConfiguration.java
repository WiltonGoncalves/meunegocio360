package br.com.meunegocio360.core.shared.config;

import br.com.meunegocio360.core.auth.domain.AuthPolicy;
import java.time.Clock;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class CoreConfiguration {

  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  AuthPolicy authPolicy(AuthProperties properties) {
    return new AuthPolicy(
        properties.maxTentativas(),
        Duration.ofMinutes(properties.bloqueioMinutos()),
        Duration.ofMinutes(properties.jwt().expiracaoMinutos()),
        Duration.ofDays(properties.refresh().expiracaoDias()));
  }
}
