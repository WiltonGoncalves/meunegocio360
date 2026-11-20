package br.com.meunegocio360.core.cliente.application;

import br.com.meunegocio360.core.cliente.api.AlterarSituacaoClienteRequest;
import br.com.meunegocio360.core.cliente.api.AtualizarClienteRequest;
import br.com.meunegocio360.core.cliente.api.ClienteRequest;
import br.com.meunegocio360.core.cliente.api.ClienteResponse;
import br.com.meunegocio360.core.cliente.api.PaginaResponse;
import br.com.meunegocio360.core.cliente.domain.Cliente;
import br.com.meunegocio360.core.cliente.domain.ClienteRepositorio;
import br.com.meunegocio360.core.cliente.domain.ConflitoCliente;
import br.com.meunegocio360.core.cliente.domain.PaginaClientes;
import br.com.meunegocio360.core.cliente.domain.SituacaoCliente;
import br.com.meunegocio360.core.cliente.domain.VerificadorVinculoCliente;
import br.com.meunegocio360.core.empresa.application.EmpresaAtiva;
import br.com.meunegocio360.core.shared.error.ErroDeNegocio;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {

  private static final String DOCUMENT_DUPLICATE_CODE = "CUSTOMER_DOCUMENT_DUPLICATE";
  private final ClienteRepositorio clienteRepositorio;
  private final EmpresaAtiva empresaAtiva;
  private final List<VerificadorVinculoCliente> verificadoresVinculo;
  private final Clock clock;

  public ClienteService(
      ClienteRepositorio clienteRepositorio,
      EmpresaAtiva empresaAtiva,
      List<VerificadorVinculoCliente> verificadoresVinculo,
      Clock clock) {
    this.clienteRepositorio = clienteRepositorio;
    this.empresaAtiva = empresaAtiva;
    this.verificadoresVinculo = verificadoresVinculo;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public PaginaResponse<ClienteResponse> listar(
      String q, SituacaoCliente situacao, int page, int size, String sort) {
    validarPaginacao(page, size, sort);
    String busca = normalizarBusca(q);
    String buscaDocumento = digitosOuNulo(busca);
    PaginaClientes resultado =
        clienteRepositorio.listar(
            empresaAtiva.obterId(), situacao, busca, buscaDocumento, page, size, sort);
    return new PaginaResponse<>(
        resultado.content().stream().map(ClienteService::resposta).toList(),
        resultado.page(),
        resultado.size(),
        resultado.totalElements(),
        resultado.totalPages());
  }

  @Transactional
  public ClienteResponse cadastrar(ClienteRequest request) {
    UUID empresaId = empresaAtiva.obterId();
    String documento = normalizarDocumento(request.documento());
    if (clienteRepositorio.buscarPorDocumentoEEmpresa(documento, empresaId).isPresent()) {
      throw documentoDuplicado();
    }

    Cliente cliente =
        new Cliente(
            empresaId,
            request.tipoPessoa(),
            request.nome(),
            documento,
            request.email(),
            request.telefone(),
            clock.instant());
    return salvar(cliente);
  }

  @Transactional(readOnly = true)
  public ClienteResponse consultar(UUID id) {
    return resposta(buscarPorIdEEmpresa(id, empresaAtiva.obterId()));
  }

  @Transactional
  public ClienteResponse atualizar(UUID id, AtualizarClienteRequest request) {
    UUID empresaId = empresaAtiva.obterId();
    Cliente cliente = buscarPorIdEEmpresa(id, empresaId);
    validarVersao(cliente, request.versao());
    String documento = normalizarDocumento(request.documento());
    clienteRepositorio
        .buscarPorDocumentoEEmpresaExcetoId(documento, empresaId, id)
        .ifPresent(ClienteService::lancarDocumentoDuplicado);

    cliente.atualizar(
        request.tipoPessoa(),
        request.nome(),
        documento,
        request.email(),
        request.telefone(),
        clock.instant());
    return salvar(cliente);
  }

  @Transactional
  public ClienteResponse alterarSituacao(UUID id, AlterarSituacaoClienteRequest request) {
    UUID empresaId = empresaAtiva.obterId();
    Cliente cliente = buscarPorIdEEmpresa(id, empresaId);
    validarVersao(cliente, request.versao());
    cliente.alterarSituacao(request.ativo(), clock.instant());
    return salvar(cliente);
  }

  @Transactional
  public void excluir(UUID id) {
    Cliente cliente = buscarPorIdEEmpresa(id, empresaAtiva.obterId());
    if (verificadoresVinculo.stream().anyMatch(verificador -> verificador.possuiVinculo(id))) {
      throw new ErroDeNegocio(
          422,
          "CUSTOMER_HAS_HISTORY",
          "Cliente possui histórico",
          "O cliente possui vendas ou ordens de serviço vinculadas e não pode ser excluído. Inative o cliente.");
    }
    clienteRepositorio.excluir(cliente);
  }

  private ClienteResponse salvar(Cliente cliente) {
    try {
      return resposta(clienteRepositorio.salvar(cliente));
    } catch (ConflitoCliente conflito) {
      throw documentoDuplicado();
    } catch (ObjectOptimisticLockingFailureException conflito) {
      throw versaoDesatualizada();
    }
  }

  private Cliente buscarPorIdEEmpresa(UUID id, UUID empresaId) {
    return clienteRepositorio
        .buscarPorIdEEmpresa(id, empresaId)
        .orElseThrow(
            () ->
                new ErroDeNegocio(
                    404,
                    "CUSTOMER_NOT_FOUND",
                    "Cliente não encontrado",
                    "O cliente não existe na empresa ativa."));
  }

  private static void validarVersao(Cliente cliente, long versaoRecebida) {
    if (cliente.getVersao() != versaoRecebida) {
      throw versaoDesatualizada();
    }
  }

  private static void validarPaginacao(int page, int size, String sort) {
    if (page < 0 || size < 1 || size > 100) {
      throw new ErroDeNegocio(
          400,
          "VALIDATION_ERROR",
          "Dados inválidos",
          "page deve ser maior ou igual a 0 e size deve estar entre 1 e 100.");
    }
    if (!"nome".equals(sort) && !"criadoEm".equals(sort)) {
      throw new ErroDeNegocio(
          400,
          "VALIDATION_ERROR",
          "Dados inválidos",
          "sort deve ser um dos campos permitidos: nome, criadoEm.");
    }
  }

  private static String normalizarBusca(String q) {
    if (q == null || q.isBlank()) {
      return null;
    }
    return q.trim();
  }

  private static String digitosOuNulo(String value) {
    if (value == null) {
      return null;
    }
    String digitos = value.replaceAll("\\D", "");
    return digitos.isEmpty() ? null : digitos;
  }

  private static String normalizarDocumento(String documento) {
    return documento.replaceAll("\\D", "");
  }

  private static void lancarDocumentoDuplicado(Cliente cliente) {
    throw documentoDuplicado();
  }

  private static ErroDeNegocio documentoDuplicado() {
    return new ErroDeNegocio(
        409,
        DOCUMENT_DUPLICATE_CODE,
        "Documento já cadastrado",
        "Já existe um cliente com este documento nesta empresa; ele pode estar inativo.");
  }

  private static ErroDeNegocio versaoDesatualizada() {
    return new ErroDeNegocio(
        409,
        "VERSION_CONFLICT",
        "Conflito de versão",
        "O cliente foi alterado por outra operação. Consulte os dados atuais e tente novamente.");
  }

  private static ClienteResponse resposta(Cliente cliente) {
    return new ClienteResponse(
        cliente.getId(),
        cliente.getTipoPessoa(),
        cliente.getNome(),
        cliente.getDocumento(),
        cliente.getEmail(),
        cliente.getTelefone(),
        cliente.isAtivo(),
        cliente.getCriadoEm(),
        cliente.getAtualizadoEm(),
        cliente.getVersao());
  }
}
