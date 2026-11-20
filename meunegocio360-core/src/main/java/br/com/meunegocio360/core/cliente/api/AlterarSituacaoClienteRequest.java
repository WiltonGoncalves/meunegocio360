package br.com.meunegocio360.core.cliente.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AlterarSituacaoClienteRequest(
    @NotNull(message = "é obrigatório") Boolean ativo,
    @NotNull(message = "é obrigatório") @PositiveOrZero(message = "não pode ser negativo")
        Long versao) {}
