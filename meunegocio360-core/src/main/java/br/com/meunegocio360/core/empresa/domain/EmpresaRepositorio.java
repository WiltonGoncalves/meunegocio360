package br.com.meunegocio360.core.empresa.domain;

import java.util.Optional;
import java.util.UUID;

public interface EmpresaRepositorio {

  Optional<Empresa> buscarAtiva();

  boolean existeAtiva();

  Optional<Empresa> buscarPorId(UUID id);

  Optional<Empresa> buscarPorCnpj(String cnpj);

  Empresa salvar(Empresa empresa);
}
