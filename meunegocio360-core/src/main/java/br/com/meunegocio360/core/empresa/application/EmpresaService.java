package br.com.meunegocio360.core.empresa.application;

import br.com.meunegocio360.core.empresa.api.AlterarSituacaoEmpresaRequest;
import br.com.meunegocio360.core.empresa.api.AtualizarEmpresaRequest;
import br.com.meunegocio360.core.empresa.api.CriarEmpresaRequest;
import br.com.meunegocio360.core.empresa.api.EmpresaResponse;
import br.com.meunegocio360.core.empresa.domain.ConflitoEmpresa;
import br.com.meunegocio360.core.empresa.domain.Empresa;
import br.com.meunegocio360.core.empresa.domain.EmpresaRepositorio;
import br.com.meunegocio360.core.shared.error.ErroDeNegocio;
import java.time.Clock;
import java.util.UUID;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmpresaService {

  private final EmpresaRepositorio empresaRepositorio;
  private final Clock clock;

  public EmpresaService(EmpresaRepositorio empresaRepositorio, Clock clock) {
    this.empresaRepositorio = empresaRepositorio;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public EmpresaResponse consultarAtiva() {
    return empresaRepositorio
        .buscarAtiva()
        .map(EmpresaService::resposta)
        .orElseThrow(
            () ->
                new ErroDeNegocio(
                    404,
                    "COMPANY_NOT_REGISTERED",
                    "Empresa não cadastrada",
                    "Nenhuma empresa ativa está cadastrada."));
  }

  @Transactional
  public EmpresaResponse cadastrar(CriarEmpresaRequest request) {
    if (empresaRepositorio.existeAtiva()) {
      throw empresaAtiva();
    }
    if (empresaRepositorio.buscarPorCnpj(request.cnpj()).isPresent()) {
      throw cnpjDuplicado();
    }

    Empresa empresa =
        new Empresa(
            request.razaoSocial(),
            request.nomeFantasia(),
            request.cnpj(),
            request.email(),
            request.telefone(),
            request.fusoHorario(),
            request.moeda(),
            clock.instant());
    try {
      return resposta(empresaRepositorio.salvar(empresa));
    } catch (ConflitoEmpresa conflito) {
      throw traduzir(conflito);
    }
  }

  @Transactional
  public EmpresaResponse atualizar(UUID id, AtualizarEmpresaRequest request) {
    Empresa empresa = buscarPorId(id);
    validarVersao(empresa, request.versao());
    validarCnpjDisponivel(request.cnpj(), id);

    empresa.atualizar(
        request.razaoSocial(),
        request.nomeFantasia(),
        request.cnpj(),
        request.email(),
        request.telefone(),
        request.fusoHorario(),
        request.moeda(),
        clock.instant());
    return salvarComControleDeConcorrencia(empresa);
  }

  @Transactional
  public EmpresaResponse alterarSituacao(UUID id, AlterarSituacaoEmpresaRequest request) {
    Empresa empresa = buscarPorId(id);
    validarVersao(empresa, request.versao());
    boolean novaSituacao = request.ativa();

    if (novaSituacao && !empresa.isAtiva()) {
      boolean outraAtiva =
          empresaRepositorio
              .buscarAtiva()
              .filter(ativa -> !ativa.getId().equals(empresa.getId()))
              .isPresent();
      if (outraAtiva) {
        throw empresaAtiva();
      }
    }

    empresa.alterarSituacao(novaSituacao, clock.instant());
    return salvarComControleDeConcorrencia(empresa);
  }

  private Empresa buscarPorId(UUID id) {
    return empresaRepositorio
        .buscarPorId(id)
        .orElseThrow(
            () ->
                new ErroDeNegocio(
                    404,
                    "COMPANY_NOT_FOUND",
                    "Empresa não encontrada",
                    "A empresa solicitada não existe."));
  }

  private void validarCnpjDisponivel(String cnpj, UUID id) {
    empresaRepositorio
        .buscarPorCnpj(cnpj)
        .filter(empresa -> !empresa.getId().equals(id))
        .ifPresent(
            empresa -> {
              throw cnpjDuplicado();
            });
  }

  private EmpresaResponse salvarComControleDeConcorrencia(Empresa empresa) {
    try {
      return resposta(empresaRepositorio.salvar(empresa));
    } catch (ConflitoEmpresa conflito) {
      throw traduzir(conflito);
    } catch (ObjectOptimisticLockingFailureException exception) {
      throw versaoDesatualizada();
    }
  }

  private static void validarVersao(Empresa empresa, long versaoRecebida) {
    if (empresa.getVersao() != versaoRecebida) {
      throw versaoDesatualizada();
    }
  }

  private static ErroDeNegocio traduzir(ConflitoEmpresa conflito) {
    return switch (conflito.getTipo()) {
      case CNPJ_DUPLICADO -> cnpjDuplicado();
      case EMPRESA_ATIVA -> empresaAtiva();
    };
  }

  private static ErroDeNegocio cnpjDuplicado() {
    return new ErroDeNegocio(
        409,
        "COMPANY_CNPJ_DUPLICATE",
        "CNPJ já cadastrado",
        "Já existe uma empresa cadastrada com este CNPJ.");
  }

  private static ErroDeNegocio empresaAtiva() {
    return new ErroDeNegocio(
        409,
        "COMPANY_ALREADY_ACTIVE",
        "Já existe uma empresa ativa",
        "Inative a empresa atual antes de cadastrar ou reativar outra.");
  }

  private static ErroDeNegocio versaoDesatualizada() {
    return new ErroDeNegocio(
        409,
        "VERSION_CONFLICT",
        "Conflito de versão",
        "A empresa foi alterada por outra operação. Consulte os dados atuais e tente novamente.");
  }

  private static EmpresaResponse resposta(Empresa empresa) {
    return new EmpresaResponse(
        empresa.getId(),
        empresa.getRazaoSocial(),
        empresa.getNomeFantasia(),
        empresa.getCnpj(),
        empresa.getEmail(),
        empresa.getTelefone(),
        empresa.getFusoHorario(),
        empresa.getMoeda(),
        empresa.isAtiva(),
        empresa.getCriadoEm(),
        empresa.getAtualizadoEm(),
        empresa.getVersao());
  }
}
