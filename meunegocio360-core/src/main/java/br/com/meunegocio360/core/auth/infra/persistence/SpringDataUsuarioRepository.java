package br.com.meunegocio360.core.auth.infra.persistence;

import br.com.meunegocio360.core.auth.domain.Usuario;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataUsuarioRepository extends JpaRepository<Usuario, UUID> {

  boolean existsByEmail(String email);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select u from Usuario u where u.email = :email")
  Optional<Usuario> buscarPorEmailParaAtualizacao(@Param("email") String email);
}
