package br.com.meunegocio360.core.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

  @Bean
  OpenAPI openApi() {
    return new OpenAPI()
        .components(
            new Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
        .info(
            new Info()
                .title("MeuNegocio360 - Núcleo")
                .version("v1")
                .description(
                    "API de identidade, autenticação e cadastro da empresa do Núcleo. "
                        + "O componente EmpresaAtiva disponibiliza o contexto da empresa "
                        + "no servidor; operações de negócio exigem empresa ativa "
                        + "(COMPANY_NOT_ACTIVE, HTTP 422)."));
  }
}
