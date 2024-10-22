package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CompensacionLabNivelVigenciaEntity;

public interface CompensacionLabNivelVigenciaDAO extends JpaRepository<CompensacionLabNivelVigenciaEntity, Long>, JpaSpecificationExecutor<CompensacionLabNivelVigenciaEntity>{
    
    @Modifying
    @Query(value = "update CompensacionLabNivelVigenciaEntity clnv set clnv clnv.idNivel =:idNivel, clnv.idCompensacionLaboral =:idCompensacionLaboral, " +
                    "clnv.idEscalaSalarial =:idEscalaSalarial, clnv.idVigencia =:idVigencia, clnv.idRegla =:idRegla, clnv.idVariable =:idVariable, " +
                    "clnv.fechaCambio =:fechaCambio, clnv.registradoPor =:registradoPor where clnv.id =:id")
    int update (@Param("idNivel") Long idNivel,
                @Param("idCompensacionLaboral") Long idCompensacionLaboral,
                @Param("idEscalaSalarial") Long idEscalaSalarial,
                @Param("idVigencia") Long idVigencia,
                @Param("idRegla") Long idRegla,
                @Param("idVariable") Long idVariable,
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_COMPENSACIONLABNIVELVIGENCIA(?1, ?2)",nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);
}
