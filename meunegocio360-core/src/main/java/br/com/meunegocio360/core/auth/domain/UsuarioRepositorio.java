package br.com.meunegocio360.core.auth.domain;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepositorio {

  Optional<Usuario> buscarPorEmailParaAtualizacao(String email);

  Optional<Usuario> buscarPorId(UUID id);

  Usuario salvar(Usuario usuario);

  boolean existePorEmail(String email);
}
