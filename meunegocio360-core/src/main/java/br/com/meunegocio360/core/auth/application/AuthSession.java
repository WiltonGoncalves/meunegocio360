package br.com.meunegocio360.core.auth.application;

public record AuthSession(
    String accessToken, long expiresIn, String rawRefreshToken, AuthenticatedUser usuario) {}
