package br.com.meunegocio360.core.cliente.api;

import br.com.meunegocio360.core.cliente.application.ClienteService;
import br.com.meunegocio360.core.cliente.domain.SituacaoCliente;
import br.com.meunegocio360.core.shared.error.ProblemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/clientes")
@Tag(name = "Clientes", description = "Cadastro e consulta de clientes da empresa ativa.")
@SecurityRequirement(name = "bearerAuth")
public class ClienteController {

  private final ClienteService clienteService;

  public ClienteController(ClienteService clienteService) {
    this.clienteService = clienteService;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_COMERCIAL')")
  @Operation(summary = "Listar clientes paginados, com busca e filtro por situação.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Página de clientes.",
        content = @Content(schema = @Schema(implementation = PaginaResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "O perfil financeiro não pode acessar clientes.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class))),
    @ApiResponse(
        responseCode = "422",
        description = "Não há empresa ativa.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class),
                examples =
                    @ExampleObject(value = "{\"code\":\"COMPANY_NOT_ACTIVE\",\"status\":422}")))
  })
  public PaginaResponse<ClienteResponse> listar(
      @Parameter(description = "Busca por nome (ignorando acentos e caixa) ou documento.")
          @RequestParam(required = false)
          String q,
      @Parameter(description = "Situação a listar; o padrão é ATIVO.")
          @RequestParam(defaultValue = "ATIVO")
          SituacaoCliente situacao,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @Parameter(description = "Campo permitido para ordenação: nome ou criadoEm.")
          @RequestParam(defaultValue = "nome")
          String sort) {
    return clienteService.listar(q, situacao, page, size, sort);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_COMERCIAL')")
  @Operation(summary = "Cadastrar cliente pessoa física ou jurídica.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Cliente criado."),
    @ApiResponse(
        responseCode = "400",
        description = "Dados inválidos.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class),
                examples =
                    @ExampleObject(value = "{\"code\":\"VALIDATION_ERROR\",\"status\":400}"))),
    @ApiResponse(
        responseCode = "403",
        description = "Perfil sem permissão.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"ACCESS_DENIED\",\"status\":403}"))),
    @ApiResponse(
        responseCode = "409",
        description = "Documento já cadastrado nesta empresa.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class),
                examples =
                    @ExampleObject(
                        value = "{\"code\":\"CUSTOMER_DOCUMENT_DUPLICATE\",\"status\":409}"))),
    @ApiResponse(
        responseCode = "422",
        description = "Não há empresa ativa.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class),
                examples =
                    @ExampleObject(value = "{\"code\":\"COMPANY_NOT_ACTIVE\",\"status\":422}")))
  })
  public ResponseEntity<ClienteResponse> cadastrar(@Valid @RequestBody ClienteRequest request) {
    ClienteResponse cliente = clienteService.cadastrar(request);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .path("/{id}")
            .buildAndExpand(cliente.id())
            .toUri();
    return ResponseEntity.created(location).body(cliente);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_COMERCIAL')")
  @Operation(summary = "Consultar cliente da empresa ativa.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Cliente encontrado.",
        content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Perfil sem permissão.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Cliente inexistente nesta empresa.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class),
                examples =
                    @ExampleObject(value = "{\"code\":\"CUSTOMER_NOT_FOUND\",\"status\":404}")))
  })
  public ClienteResponse consultar(@PathVariable UUID id) {
    return clienteService.consultar(id);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_COMERCIAL')")
  @Operation(summary = "Atualizar dados de cliente com controle otimista de versão.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Cliente atualizado."),
    @ApiResponse(
        responseCode = "400",
        description = "Dados inválidos.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Perfil sem permissão.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Cliente inexistente nesta empresa.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "Documento duplicado ou versão desatualizada.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class)))
  })
  public ClienteResponse atualizar(
      @PathVariable UUID id, @Valid @RequestBody AtualizarClienteRequest request) {
    return clienteService.atualizar(id, request);
  }

  @PatchMapping("/{id}/situacao")
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_COMERCIAL')")
  @Operation(summary = "Inativar ou reativar cliente.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Situação atualizada."),
    @ApiResponse(
        responseCode = "403",
        description = "Perfil sem permissão.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Cliente inexistente nesta empresa.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "Versão desatualizada.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class)))
  })
  public ClienteResponse alterarSituacao(
      @PathVariable UUID id, @Valid @RequestBody AlterarSituacaoClienteRequest request) {
    return clienteService.alterarSituacao(id, request);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR_COMERCIAL')")
  @Operation(summary = "Excluir cliente somente se nenhum vínculo histórico existir.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Cliente excluído."),
    @ApiResponse(
        responseCode = "403",
        description = "Perfil sem permissão.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Cliente inexistente nesta empresa.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class))),
    @ApiResponse(
        responseCode = "422",
        description = "Cliente possui histórico e não pode ser excluído.",
        content =
            @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemResponse.class),
                examples =
                    @ExampleObject(value = "{\"code\":\"CUSTOMER_HAS_HISTORY\",\"status\":422}")))
  })
  public ResponseEntity<Void> excluir(@PathVariable UUID id) {
    clienteService.excluir(id);
    return ResponseEntity.noContent().build();
  }
}
