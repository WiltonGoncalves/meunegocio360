package br.com.meunegocio360.core.cliente.domain;

import java.util.UUID;

public interface VerificadorVinculoCliente {

  boolean possuiVinculo(UUID clienteId);
}
