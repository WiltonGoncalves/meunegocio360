package br.com.meunegocio360.core.cliente.api;

import br.com.meunegocio360.core.cliente.domain.TipoPessoa;
import java.time.Instant;
import java.util.UUID;

public record ClienteResponse(
    UUID id,
    TipoPessoa tipoPessoa,
    String nome,
    String documento,
    String email,
    String telefone,
    boolean ativo,
    Instant criadoEm,
    Instant atualizadoEm,
    long versao) {}
