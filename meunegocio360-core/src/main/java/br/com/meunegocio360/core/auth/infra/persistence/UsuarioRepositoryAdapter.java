package br.com.meunegocio360.core.auth.infra.persistence;

import br.com.meunegocio360.core.auth.domain.Usuario;
import br.com.meunegocio360.core.auth.domain.port.UsuarioRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class UsuarioRepositoryAdapter implements UsuarioRepository {

  private final SpringDataUsuarioRepository repository;

  public UsuarioRepositoryAdapter(SpringDataUsuarioRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<Usuario> buscarPorEmail(String email) {
    return repository.findByEmail(email).map(UsuarioRepositoryAdapter::toDomain);
  }

  @Override
  public Optional<Usuario> buscarPorEmailParaAtualizacao(String email) {
    return repository.findByEmailForUpdate(email).map(UsuarioRepositoryAdapter::toDomain);
  }

  @Override
  public Optional<Usuario> buscarPorId(UUID id) {
    return repository.findById(id).map(UsuarioRepositoryAdapter::toDomain);
  }

  @Override
  public Optional<Usuario> buscarPorIdParaAtualizacao(UUID id) {
    return repository.findByIdForUpdate(id).map(UsuarioRepositoryAdapter::toDomain);
  }

  @Override
  public Usuario salvar(Usuario usuario) {
    UsuarioJpaEntity entity =
        new UsuarioJpaEntity(
            usuario.id(),
            usuario.nome(),
            usuario.email(),
            usuario.senhaHash(),
            usuario.perfil(),
            usuario.ativo(),
            usuario.tentativasFalhas(),
            usuario.bloqueadoAte(),
            usuario.ultimoLoginEm(),
            usuario.criadoEm(),
            usuario.atualizadoEm(),
            usuario.versao());
    return toDomain(repository.saveAndFlush(entity));
  }

  private static Usuario toDomain(UsuarioJpaEntity entity) {
    return new Usuario(
        entity.getId(),
        entity.getNome(),
        entity.getEmail(),
        entity.getSenhaHash(),
        entity.getPerfil(),
        entity.isAtivo(),
        entity.getTentativasFalhas(),
        entity.getBloqueadoAte(),
        entity.getUltimoLoginEm(),
        entity.getCriadoEm(),
        entity.getAtualizadoEm(),
        entity.getVersao());
  }
}
