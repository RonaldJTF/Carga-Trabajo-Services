package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.TipologiaAccionEntity;

public interface TipologiaAccionDAO extends JpaRepository<TipologiaAccionEntity, Long>, JpaSpecificationExecutor<TipologiaAccionEntity>{

    @Modifying
    @Query(value = "update TipologiaAccionEntity ta set ta.idTipologia =:idTipologia, ta.idAccion =:idAccion, " + 
                    "ta.fechaCambio = :fechaCambio,  ta.registradoPor = :registradoPor where ta.id = :id")
    int update (@Param("idTipologia") Long idTipologia,
                @Param("idAccion") Long idAccion,
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_TIPOLOGIAACCION(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);
}
