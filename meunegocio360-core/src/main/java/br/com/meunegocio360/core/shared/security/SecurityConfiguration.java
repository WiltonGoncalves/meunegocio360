package br.com.meunegocio360.core.shared.security;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfiguration {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http, Environment environment)
      throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authorize -> {
              authorize
                  .requestMatchers(
                      "/api/v1/auth/login",
                      "/api/v1/auth/refresh",
                      "/api/v1/auth/logout",
                      "/actuator/health")
                  .permitAll();
              if (!environment.acceptsProfiles(Profiles.of("prod"))) {
                authorize
                    .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**")
                    .permitAll();
              }
              authorize.anyRequest().authenticated();
            })
        .oauth2ResourceServer(
            resourceServer ->
                resourceServer
                    .jwt(
                        jwt ->
                            jwt.jwtAuthenticationConverter(
                                token ->
                                    new JwtAuthenticationToken(
                                        token,
                                        List.of(
                                            new SimpleGrantedAuthority(
                                                "ROLE_" + token.getClaimAsString("perfil"))))))
                    .authenticationEntryPoint(new ProblemAuthenticationEntryPoint()))
        .exceptionHandling(
            exceptions ->
                exceptions
                    .authenticationEntryPoint(new ProblemAuthenticationEntryPoint())
                    .accessDeniedHandler(new ProblemAccessDeniedHandler()));
    return http.build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource(
      @Value("${app.auth.cors.allowed-origins:}") String origins) {
    CorsConfiguration configuration = new CorsConfiguration();
    List<String> allowedOrigins =
        Arrays.stream(origins.split(","))
            .map(String::trim)
            .filter(origin -> !origin.isEmpty())
            .toList();
    if (allowedOrigins.stream().anyMatch("*"::equals)) {
      throw new IllegalStateException("CORS_ALLOWED_ORIGINS não pode conter curinga.");
    }
    if (allowedOrigins.isEmpty()) {
      throw new IllegalStateException("CORS_ALLOWED_ORIGINS deve informar ao menos uma origem.");
    }
    allowedOrigins.forEach(SecurityConfiguration::validarOrigem);
    configuration.setAllowedOrigins(allowedOrigins);
    configuration.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Request-Id"));
    configuration.setExposedHeaders(List.of("X-Request-Id", "Retry-After", "WWW-Authenticate"));
    configuration.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", configuration);
    return source;
  }

  private static void validarOrigem(String origem) {
    URI uri;
    try {
      uri = URI.create(origem);
    } catch (IllegalArgumentException exception) {
      throw new IllegalStateException(
          "CORS_ALLOWED_ORIGINS contém uma origem inválida.", exception);
    }
    if ((!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())))
        || uri.getHost() == null
        || uri.getRawUserInfo() != null
        || uri.getRawQuery() != null
        || uri.getRawFragment() != null
        || (uri.getRawPath() != null && !uri.getRawPath().isEmpty())) {
      throw new IllegalStateException(
          "CORS_ALLOWED_ORIGINS deve conter origens HTTP(S) sem caminho.");
    }
  }
}
