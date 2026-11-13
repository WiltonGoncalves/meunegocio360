package br.com.meunegocio360.core.auth.infra.persistence;

import br.com.meunegocio360.core.auth.domain.RefreshToken;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataRefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select t from RefreshToken t where t.tokenHash = :hash")
  Optional<RefreshToken> buscarPorHashParaAtualizacao(@Param("hash") String hash);

  @Modifying
  @Query(
      "update RefreshToken t set t.revogadoEm = :agora "
          + "where t.familiaId = :familiaId and t.revogadoEm is null")
  void revogarFamilia(@Param("familiaId") UUID familiaId, @Param("agora") Instant agora);

  @Modifying
  @Query(
      "update RefreshToken t set t.revogadoEm = :agora "
          + "where t.usuarioId = :usuarioId and t.revogadoEm is null")
  void revogarTodasDoUsuario(@Param("usuarioId") UUID usuarioId, @Param("agora") Instant agora);

  @Modifying
  @Query(
      "delete from RefreshToken t where "
          + "(t.expiraEm < :limite) or (t.revogadoEm is not null and t.revogadoEm < :limite)")
  void removerExpiradosOuRevogadosAntesDe(@Param("limite") Instant limite);
}
