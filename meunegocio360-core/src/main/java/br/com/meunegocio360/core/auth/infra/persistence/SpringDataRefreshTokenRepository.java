package br.com.meunegocio360.core.auth.infra.persistence;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataRefreshTokenRepository
    extends JpaRepository<RefreshTokenJpaEntity, UUID> {

  Optional<RefreshTokenJpaEntity> findByTokenHash(String tokenHash);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select t from RefreshTokenJpaEntity t where t.tokenHash = :tokenHash")
  Optional<RefreshTokenJpaEntity> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

  @Modifying
  @Query(
      """
			update RefreshTokenJpaEntity t
			set t.revogadoEm = :revogadoEm
			where t.familiaId = :familiaId and t.revogadoEm is null
			""")
  int revokeFamily(@Param("familiaId") UUID familiaId, @Param("revogadoEm") Instant revogadoEm);

  @Modifying
  @Query(
      """
			update RefreshTokenJpaEntity t
			set t.revogadoEm = :revogadoEm
			where t.usuarioId = :usuarioId and t.revogadoEm is null
			""")
  int revokeAllForUser(@Param("usuarioId") UUID usuarioId, @Param("revogadoEm") Instant revogadoEm);

  @Modifying
  @Query(
      """
			delete from RefreshTokenJpaEntity t
			where t.expiraEm < :limite or t.revogadoEm < :limite
			""")
  int deleteOldTokens(@Param("limite") Instant limite);
}
