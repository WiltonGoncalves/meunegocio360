package br.com.meunegocio360.core.empresa.api;

import br.com.meunegocio360.core.empresa.application.EmpresaService;
import br.com.meunegocio360.core.shared.error.ProblemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/empresa")
@Tag(name = "Empresa", description = "Cadastro e parâmetros da empresa ativa do Núcleo.")
@SecurityRequirement(name = "bearerAuth")
public class EmpresaController {

  private final EmpresaService empresaService;

  public EmpresaController(EmpresaService empresaService) {
    this.empresaService = empresaService;
  }

  @GetMapping
  @Operation(summary = "Consultar a empresa ativa.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Empresa ativa.",
        content =
            @Content(
                schema = @Schema(implementation = EmpresaResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"id\":\"0b1d6a64-6a0e-4c1c-a2f4-5f6a2d1f9a10\",\"razaoSocial\":\"Comércio Exemplo Ltda\",\"nomeFantasia\":\"Exemplo Informática\",\"cnpj\":\"11222333000181\",\"email\":\"contato@exemplo.com.br\",\"telefone\":\"11987654321\",\"fusoHorario\":\"America/Sao_Paulo\",\"moeda\":\"BRL\",\"ativa\":true,\"criadoEm\":\"2026-11-18T14:30:00Z\",\"atualizadoEm\":\"2026-11-18T14:30:00Z\",\"versao\":0}"))),
    @ApiResponse(
        responseCode = "404",
        description = "Ainda não há empresa cadastrada.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"type\":\"https://meunegocio360.com.br/problemas/company_not_registered\",\"title\":\"Empresa não cadastrada\",\"status\":404,\"detail\":\"Nenhuma empresa ativa está cadastrada.\",\"instance\":\"/api/v1/empresa\",\"code\":\"COMPANY_NOT_REGISTERED\",\"traceId\":\"00000000-0000-4000-8000-000000000000\",\"errors\":[]}")))
  })
  public EmpresaResponse consultar() {
    return empresaService.consultarAtiva();
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Cadastrar a empresa, ativando-a.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Empresa cadastrada.",
        headers = @Header(name = "Location", description = "URI da empresa criada."),
        content =
            @Content(
                schema = @Schema(implementation = EmpresaResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"id\":\"0b1d6a64-6a0e-4c1c-a2f4-5f6a2d1f9a10\",\"razaoSocial\":\"Comércio Exemplo Ltda\",\"nomeFantasia\":null,\"cnpj\":\"11222333000181\",\"email\":null,\"telefone\":null,\"fusoHorario\":\"America/Sao_Paulo\",\"moeda\":\"BRL\",\"ativa\":true,\"criadoEm\":\"2026-11-18T14:30:00Z\",\"atualizadoEm\":\"2026-11-18T14:30:00Z\",\"versao\":0}"))),
    @ApiResponse(
        responseCode = "400",
        description = "Dados inválidos.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"type\":\"https://meunegocio360.com.br/problemas/validation_error\",\"title\":\"Dados inválidos\",\"status\":400,\"detail\":\"Um ou mais campos são inválidos.\",\"instance\":\"/api/v1/empresa\",\"code\":\"VALIDATION_ERROR\",\"traceId\":\"00000000-0000-4000-8000-000000000000\",\"errors\":[{\"field\":\"cnpj\",\"message\":\"deve ser um CNPJ válido com 14 dígitos\"}]}"))),
    @ApiResponse(
        responseCode = "403",
        description = "Perfil sem permissão para escrita.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"type\":\"https://meunegocio360.com.br/problemas/access_denied\",\"title\":\"Acesso negado\",\"status\":403,\"detail\":\"Você não tem permissão para executar esta operação.\",\"instance\":\"/api/v1/empresa\",\"code\":\"ACCESS_DENIED\",\"traceId\":\"00000000-0000-4000-8000-000000000000\",\"errors\":[]}"))),
    @ApiResponse(
        responseCode = "409",
        description = "CNPJ duplicado ou já existe empresa ativa.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"type\":\"https://meunegocio360.com.br/problemas/company_already_active\",\"title\":\"Já existe uma empresa ativa\",\"status\":409,\"detail\":\"Inative a empresa atual antes de cadastrar ou reativar outra.\",\"instance\":\"/api/v1/empresa\",\"code\":\"COMPANY_ALREADY_ACTIVE\",\"traceId\":\"00000000-0000-4000-8000-000000000000\",\"errors\":[]}")))
  })
  public ResponseEntity<EmpresaResponse> cadastrar(
      @Valid @RequestBody CriarEmpresaRequest request) {
    EmpresaResponse empresa = empresaService.cadastrar(request);
    URI location = ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri();
    return ResponseEntity.created(location).body(empresa);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Atualizar os dados da empresa usando controle otimista de versão.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Empresa atualizada.",
        content =
            @Content(
                schema = @Schema(implementation = EmpresaResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"id\":\"0b1d6a64-6a0e-4c1c-a2f4-5f6a2d1f9a10\",\"razaoSocial\":\"Comércio Exemplo Ltda\",\"nomeFantasia\":\"Exemplo Informática\",\"cnpj\":\"11222333000181\",\"email\":null,\"telefone\":null,\"fusoHorario\":\"America/Sao_Paulo\",\"moeda\":\"BRL\",\"ativa\":true,\"criadoEm\":\"2026-11-18T14:30:00Z\",\"atualizadoEm\":\"2026-11-18T14:30:00Z\",\"versao\":1}"))),
    @ApiResponse(
        responseCode = "400",
        description = "Dados inválidos.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Perfil sem permissão para escrita.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"type\":\"https://meunegocio360.com.br/problemas/access_denied\",\"title\":\"Acesso negado\",\"status\":403,\"detail\":\"Você não tem permissão para executar esta operação.\",\"instance\":\"/api/v1/empresa/0b1d6a64-6a0e-4c1c-a2f4-5f6a2d1f9a10\",\"code\":\"ACCESS_DENIED\",\"traceId\":\"00000000-0000-4000-8000-000000000000\",\"errors\":[]}"))),
    @ApiResponse(
        responseCode = "404",
        description = "Empresa não encontrada.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "CNPJ duplicado ou versão desatualizada.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"type\":\"https://meunegocio360.com.br/problemas/version_conflict\",\"title\":\"Conflito de versão\",\"status\":409,\"detail\":\"A empresa foi alterada por outra operação. Consulte os dados atuais e tente novamente.\",\"instance\":\"/api/v1/empresa/0b1d6a64-6a0e-4c1c-a2f4-5f6a2d1f9a10\",\"code\":\"VERSION_CONFLICT\",\"traceId\":\"00000000-0000-4000-8000-000000000000\",\"errors\":[]}")))
  })
  public EmpresaResponse atualizar(
      @PathVariable UUID id, @Valid @RequestBody AtualizarEmpresaRequest request) {
    return empresaService.atualizar(id, request);
  }

  @PatchMapping("/{id}/situacao")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Inativar ou reativar a empresa.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Situação da empresa atualizada.",
        content =
            @Content(
                schema = @Schema(implementation = EmpresaResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"id\":\"0b1d6a64-6a0e-4c1c-a2f4-5f6a2d1f9a10\",\"razaoSocial\":\"Comércio Exemplo Ltda\",\"nomeFantasia\":null,\"cnpj\":\"11222333000181\",\"email\":null,\"telefone\":null,\"fusoHorario\":\"America/Sao_Paulo\",\"moeda\":\"BRL\",\"ativa\":false,\"criadoEm\":\"2026-11-18T14:30:00Z\",\"atualizadoEm\":\"2026-11-18T14:30:00Z\",\"versao\":1}"))),
    @ApiResponse(
        responseCode = "403",
        description = "Perfil sem permissão para escrita.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"type\":\"https://meunegocio360.com.br/problemas/access_denied\",\"title\":\"Acesso negado\",\"status\":403,\"detail\":\"Você não tem permissão para executar esta operação.\",\"instance\":\"/api/v1/empresa/0b1d6a64-6a0e-4c1c-a2f4-5f6a2d1f9a10/situacao\",\"code\":\"ACCESS_DENIED\",\"traceId\":\"00000000-0000-4000-8000-000000000000\",\"errors\":[]}"))),
    @ApiResponse(
        responseCode = "404",
        description = "Empresa não encontrada.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "Versão desatualizada ou outra empresa está ativa.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"type\":\"https://meunegocio360.com.br/problemas/company_already_active\",\"title\":\"Já existe uma empresa ativa\",\"status\":409,\"detail\":\"Inative a empresa atual antes de cadastrar ou reativar outra.\",\"instance\":\"/api/v1/empresa/0b1d6a64-6a0e-4c1c-a2f4-5f6a2d1f9a10/situacao\",\"code\":\"COMPANY_ALREADY_ACTIVE\",\"traceId\":\"00000000-0000-4000-8000-000000000000\",\"errors\":[]}")))
  })
  public EmpresaResponse alterarSituacao(
      @PathVariable UUID id, @Valid @RequestBody AlterarSituacaoEmpresaRequest request) {
    return empresaService.alterarSituacao(id, request);
  }
}
