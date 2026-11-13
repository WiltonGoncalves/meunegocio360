package br.com.meunegocio360.core.auth.api;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SessaoAutenticada")
public record AuthResponse(
    String tokenType, String accessToken, long expiresIn, AuthUserResponse usuario) {}
