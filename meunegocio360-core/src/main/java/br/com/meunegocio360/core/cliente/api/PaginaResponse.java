package br.com.meunegocio360.core.cliente.api;

import java.util.List;

public record PaginaResponse<T>(
    List<T> content, int page, int size, long totalElements, int totalPages) {}
