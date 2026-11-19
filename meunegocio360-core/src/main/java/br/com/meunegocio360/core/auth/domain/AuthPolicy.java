package br.com.meunegocio360.core.auth.domain;

import java.time.Duration;

public record AuthPolicy(
    int maxTentativas,
    Duration duracaoBloqueio,
    Duration duracaoAccessToken,
    Duration duracaoRefreshToken) {}
