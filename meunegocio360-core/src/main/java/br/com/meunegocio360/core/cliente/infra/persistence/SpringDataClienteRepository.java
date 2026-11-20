package br.com.meunegocio360.core.cliente.infra.persistence;

import br.com.meunegocio360.core.cliente.domain.Cliente;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataClienteRepository extends JpaRepository<Cliente, UUID> {

  @Query(
      value =
          "select c from Cliente c where c.empresaId = :empresaId "
              + "and (:ativo is null or c.ativo = :ativo) "
              + "and (:busca is null or lower(c.nome) like lower(concat('%', :busca, '%')) "
              + "or (:buscaDocumento is not null and c.documento like concat('%', :buscaDocumento, '%')))",
      countQuery =
          "select count(c) from Cliente c where c.empresaId = :empresaId "
              + "and (:ativo is null or c.ativo = :ativo) "
              + "and (:busca is null or lower(c.nome) like lower(concat('%', :busca, '%')) "
              + "or (:buscaDocumento is not null and c.documento like concat('%', :buscaDocumento, '%')))")
  Page<Cliente> listar(
      @Param("empresaId") UUID empresaId,
      @Param("ativo") Boolean ativo,
      @Param("busca") String busca,
      @Param("buscaDocumento") String buscaDocumento,
      Pageable pageable);

  Optional<Cliente> findByIdAndEmpresaId(UUID id, UUID empresaId);

  Optional<Cliente> findByDocumentoAndEmpresaId(String documento, UUID empresaId);

  Optional<Cliente> findByDocumentoAndEmpresaIdAndIdNot(String documento, UUID empresaId, UUID id);
}
