package br.com.meunegocio360.core.auth.infra.persistence;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataUsuarioRepository extends JpaRepository<UsuarioJpaEntity, UUID> {

  Optional<UsuarioJpaEntity> findByEmail(String email);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select u from UsuarioJpaEntity u where u.email = :email")
  Optional<UsuarioJpaEntity> findByEmailForUpdate(@Param("email") String email);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select u from UsuarioJpaEntity u where u.id = :id")
  Optional<UsuarioJpaEntity> findByIdForUpdate(@Param("id") UUID id);
}
