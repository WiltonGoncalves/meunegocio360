package br.com.meunegocio360.core.auth.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UsuarioTests {

  private static final Instant NOW = Instant.parse("2026-11-12T12:00:00Z");
  private static final AuthPolicy POLICY =
      new AuthPolicy(5, Duration.ofMinutes(15), Duration.ofMinutes(15), Duration.ofDays(7));

  @Test
  void locksOnFifthFailureAndUnlocksAfterDeadline() {
    Usuario usuario = usuario();
    for (int tentativa = 0; tentativa < 4; tentativa++) {
      usuario = usuario.registrarFalha(NOW, POLICY);
    }
    assertFalse(usuario.estaBloqueado(NOW));

    usuario = usuario.registrarFalha(NOW, POLICY);
    assertTrue(usuario.estaBloqueado(NOW));
    assertEquals(NOW.plus(Duration.ofMinutes(15)), usuario.bloqueadoAte());

    Usuario desbloqueado = usuario.limparBloqueioExpirado(NOW.plus(Duration.ofMinutes(15)));
    assertFalse(desbloqueado.estaBloqueado(NOW.plus(Duration.ofMinutes(15))));
    assertEquals(0, desbloqueado.tentativasFalhas());
    assertNull(desbloqueado.bloqueadoAte());
  }

  @Test
  void successfulLoginResetsFailuresAndUpdatesLastLogin() {
    Usuario usuario = usuario().registrarFalha(NOW, POLICY).registrarSucesso(NOW.plusSeconds(5));

    assertEquals(0, usuario.tentativasFalhas());
    assertNull(usuario.bloqueadoAte());
    assertEquals(NOW.plusSeconds(5), usuario.ultimoLoginEm());
  }

  private static Usuario usuario() {
    return new Usuario(
        UUID.randomUUID(),
        "Teste",
        "teste@example.com",
        "{bcrypt}hash",
        Perfil.USUARIO,
        true,
        0,
        null,
        null,
        NOW,
        NOW,
        0L);
  }
}
