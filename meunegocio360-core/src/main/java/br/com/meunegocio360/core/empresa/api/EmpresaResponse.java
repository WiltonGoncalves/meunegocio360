package br.com.meunegocio360.core.empresa.api;

import java.time.Instant;
import java.util.UUID;

public record EmpresaResponse(
    UUID id,
    String razaoSocial,
    String nomeFantasia,
    String cnpj,
    String email,
    String telefone,
    String fusoHorario,
    String moeda,
    boolean ativa,
    Instant criadoEm,
    Instant atualizadoEm,
    long versao) {}
