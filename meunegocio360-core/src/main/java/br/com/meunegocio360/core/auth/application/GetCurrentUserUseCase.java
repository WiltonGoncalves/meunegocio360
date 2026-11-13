package br.com.meunegocio360.core.auth.application;

import br.com.meunegocio360.core.auth.domain.exception.UserNotAuthenticatedException;
import br.com.meunegocio360.core.auth.domain.port.UsuarioRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetCurrentUserUseCase {

  private final UsuarioRepository usuarioRepository;

  public GetCurrentUserUseCase(UsuarioRepository usuarioRepository) {
    this.usuarioRepository = usuarioRepository;
  }

  @Transactional(readOnly = true)
  public AuthenticatedUser executar(String subject) {
    try {
      UUID usuarioId = UUID.fromString(subject);
      return usuarioRepository
          .buscarPorId(usuarioId)
          .filter(usuario -> usuario.ativo())
          .map(LoginUseCase::resumir)
          .orElseThrow(UserNotAuthenticatedException::new);
    } catch (IllegalArgumentException exception) {
      throw new UserNotAuthenticatedException();
    }
  }
}
