package br.com.meunegocio360.core.auth.domain.port;

import br.com.meunegocio360.core.auth.domain.Usuario;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository {

  Optional<Usuario> buscarPorEmail(String email);

  Optional<Usuario> buscarPorEmailParaAtualizacao(String email);

  Optional<Usuario> buscarPorId(UUID id);

  Optional<Usuario> buscarPorIdParaAtualizacao(UUID id);

  Usuario salvar(Usuario usuario);
}
