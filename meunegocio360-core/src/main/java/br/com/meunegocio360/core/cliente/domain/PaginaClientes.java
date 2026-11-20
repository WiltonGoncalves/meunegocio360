package br.com.meunegocio360.core.cliente.domain;

import java.util.List;

public record PaginaClientes(
    List<Cliente> content, int page, int size, long totalElements, int totalPages) {}
