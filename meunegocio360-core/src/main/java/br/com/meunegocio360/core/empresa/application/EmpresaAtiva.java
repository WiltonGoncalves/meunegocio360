package br.com.meunegocio360.core.empresa.application;

import br.com.meunegocio360.core.empresa.domain.EmpresaRepositorio;
import br.com.meunegocio360.core.shared.error.ErroDeNegocio;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EmpresaAtiva {

  private final EmpresaRepositorio empresaRepositorio;

  public EmpresaAtiva(EmpresaRepositorio empresaRepositorio) {
    this.empresaRepositorio = empresaRepositorio;
  }

  @Transactional(readOnly = true)
  public UUID obterId() {
    return empresaRepositorio
        .buscarAtiva()
        .map(empresa -> empresa.getId())
        .orElseThrow(
            () ->
                new ErroDeNegocio(
                    422,
                    "COMPANY_NOT_ACTIVE",
                    "Empresa não ativa",
                    "Cadastre ou reative uma empresa antes de criar dados de negócio."));
  }
}
