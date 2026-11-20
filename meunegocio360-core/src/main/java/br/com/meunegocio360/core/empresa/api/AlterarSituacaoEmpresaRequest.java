package br.com.meunegocio360.core.empresa.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AlterarSituacaoEmpresaRequest(
    @NotNull(message = "é obrigatório") Boolean ativa,
    @NotNull(message = "é obrigatório") @PositiveOrZero(message = "não pode ser negativo")
        Long versao) {}
