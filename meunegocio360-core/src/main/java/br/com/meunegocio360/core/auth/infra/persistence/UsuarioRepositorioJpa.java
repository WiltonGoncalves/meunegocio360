package br.com.meunegocio360.core.auth.infra.persistence;

import br.com.meunegocio360.core.auth.domain.Usuario;
import br.com.meunegocio360.core.auth.domain.UsuarioRepositorio;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class UsuarioRepositorioJpa implements UsuarioRepositorio {

  private final SpringDataUsuarioRepository repository;

  public UsuarioRepositorioJpa(SpringDataUsuarioRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<Usuario> buscarPorEmailParaAtualizacao(String email) {
    return repository.buscarPorEmailParaAtualizacao(email);
  }

  @Override
  public Optional<Usuario> buscarPorId(UUID id) {
    return repository.findById(id);
  }

  @Override
  public Usuario salvar(Usuario usuario) {
    return repository.save(usuario);
  }

  @Override
  public boolean existePorEmail(String email) {
    return repository.existsByEmail(email);
  }
}
