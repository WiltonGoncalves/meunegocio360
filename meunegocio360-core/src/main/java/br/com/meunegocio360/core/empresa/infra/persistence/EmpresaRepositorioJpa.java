package br.com.meunegocio360.core.empresa.infra.persistence;

import br.com.meunegocio360.core.empresa.domain.ConflitoEmpresa;
import br.com.meunegocio360.core.empresa.domain.Empresa;
import br.com.meunegocio360.core.empresa.domain.EmpresaRepositorio;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
public class EmpresaRepositorioJpa implements EmpresaRepositorio {

  private final SpringDataEmpresaRepository repository;

  public EmpresaRepositorioJpa(SpringDataEmpresaRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<Empresa> buscarAtiva() {
    return repository.findFirstByAtivaTrue();
  }

  @Override
  public boolean existeAtiva() {
    return repository.existsByAtivaTrue();
  }

  @Override
  public Optional<Empresa> buscarPorId(UUID id) {
    return repository.findById(id);
  }

  @Override
  public Optional<Empresa> buscarPorCnpj(String cnpj) {
    return repository.findByCnpj(cnpj);
  }

  @Override
  public Empresa salvar(Empresa empresa) {
    try {
      return repository.saveAndFlush(empresa);
    } catch (DataIntegrityViolationException exception) {
      String constraint = constraintName(exception);
      if (constraint != null && constraint.endsWith("uk_empresa_cnpj")) {
        throw new ConflitoEmpresa(ConflitoEmpresa.Tipo.CNPJ_DUPLICADO);
      }
      if (constraint != null && constraint.endsWith("uk_empresa_ativa_unica")) {
        throw new ConflitoEmpresa(ConflitoEmpresa.Tipo.EMPRESA_ATIVA);
      }
      throw exception;
    }
  }

  private static String constraintName(Throwable exception) {
    Throwable atual = exception;
    while (atual != null) {
      if (atual instanceof ConstraintViolationException violation) {
        if (violation.getConstraintName() != null) {
          return violation.getConstraintName();
        }
      }
      String mensagem = atual.getMessage();
      if (mensagem != null) {
        if (mensagem.contains("uk_empresa_cnpj")) {
          return "uk_empresa_cnpj";
        }
        if (mensagem.contains("uk_empresa_ativa_unica")) {
          return "uk_empresa_ativa_unica";
        }
      }
      atual = atual.getCause();
    }
    return null;
  }
}
