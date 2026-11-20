package br.com.meunegocio360.core.cliente.infra.persistence;

import br.com.meunegocio360.core.cliente.domain.Cliente;
import br.com.meunegocio360.core.cliente.domain.ClienteRepositorio;
import br.com.meunegocio360.core.cliente.domain.ConflitoCliente;
import br.com.meunegocio360.core.cliente.domain.PaginaClientes;
import br.com.meunegocio360.core.cliente.domain.SituacaoCliente;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class ClienteRepositorioJpa implements ClienteRepositorio {

  private static final String DOCUMENT_CONSTRAINT = "uk_cliente_empresa_documento";

  private final SpringDataClienteRepository repository;

  public ClienteRepositorioJpa(SpringDataClienteRepository repository) {
    this.repository = repository;
  }

  @Override
  public PaginaClientes listar(
      UUID empresaId,
      SituacaoCliente situacao,
      String buscaNome,
      String buscaDocumento,
      int page,
      int size,
      String sort) {
    Boolean ativo =
        switch (situacao) {
          case ATIVO -> true;
          case INATIVO -> false;
          case TODOS -> null;
        };
    Page<Cliente> resultado =
        repository.listar(
            empresaId, ativo, buscaNome, buscaDocumento, PageRequest.of(page, size, Sort.by(sort)));
    return new PaginaClientes(
        resultado.getContent(),
        resultado.getNumber(),
        resultado.getSize(),
        resultado.getTotalElements(),
        resultado.getTotalPages());
  }

  @Override
  public Optional<Cliente> buscarPorIdEEmpresa(UUID id, UUID empresaId) {
    return repository.findByIdAndEmpresaId(id, empresaId);
  }

  @Override
  public Optional<Cliente> buscarPorDocumentoEEmpresa(String documento, UUID empresaId) {
    return repository.findByDocumentoAndEmpresaId(documento, empresaId);
  }

  @Override
  public Optional<Cliente> buscarPorDocumentoEEmpresaExcetoId(
      String documento, UUID empresaId, UUID id) {
    return repository.findByDocumentoAndEmpresaIdAndIdNot(documento, empresaId, id);
  }

  @Override
  public Cliente salvar(Cliente cliente) {
    try {
      return repository.saveAndFlush(cliente);
    } catch (DataIntegrityViolationException exception) {
      if (violouUnicidadeDeDocumento(exception)) {
        throw new ConflitoCliente();
      }
      throw exception;
    }
  }

  @Override
  public void excluir(Cliente cliente) {
    repository.delete(cliente);
  }

  private static boolean violouUnicidadeDeDocumento(Throwable exception) {
    Throwable atual = exception;
    while (atual != null) {
      if (atual instanceof ConstraintViolationException violation
          && DOCUMENT_CONSTRAINT.equalsIgnoreCase(violation.getConstraintName())) {
        return true;
      }
      String mensagem = atual.getMessage();
      if (mensagem != null && mensagem.contains(DOCUMENT_CONSTRAINT)) {
        return true;
      }
      atual = atual.getCause();
    }
    return false;
  }
}
