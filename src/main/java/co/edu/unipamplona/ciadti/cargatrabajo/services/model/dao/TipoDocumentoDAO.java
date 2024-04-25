package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.TipoDocumentoEntity;

public interface TipoDocumentoDAO  extends JpaRepository<TipoDocumentoEntity, Long>, JpaSpecificationExecutor<TipoDocumentoEntity>{

    @Modifying
    @Query(value = "update TipoDocumentoEntity r set r.descripcion =:descripcion, r.abreviatura =:abreviatura, " +
                   "r.fechaCambio = :fechaCambio, r.registradoPor = :registradoPor where r.id=:id")
    int update (@Param("descripcion") String descripcion,
                @Param("abreviatura") String abreviatura,
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_TIPODOCUMENTO(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);
}
