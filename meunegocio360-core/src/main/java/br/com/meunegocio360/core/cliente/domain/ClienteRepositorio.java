package br.com.meunegocio360.core.cliente.domain;

import java.util.Optional;
import java.util.UUID;

public interface ClienteRepositorio {

  PaginaClientes listar(
      UUID empresaId,
      SituacaoCliente situacao,
      String buscaNome,
      String buscaDocumento,
      int page,
      int size,
      String sort);

  Optional<Cliente> buscarPorIdEEmpresa(UUID id, UUID empresaId);

  Optional<Cliente> buscarPorDocumentoEEmpresa(String documento, UUID empresaId);

  Optional<Cliente> buscarPorDocumentoEEmpresaExcetoId(String documento, UUID empresaId, UUID id);

  Cliente salvar(Cliente cliente);

  void excluir(Cliente cliente);
}
