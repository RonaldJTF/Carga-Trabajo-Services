package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CompensacionLabNivelVigValorEntity;

public interface CompensacionLabNivelVigValorDAO extends JpaRepository<CompensacionLabNivelVigValorEntity, Long>, JpaSpecificationExecutor<CompensacionLabNivelVigValorEntity> {
    @Modifying
    @Query(value = "update CompensacionLabNivelVigValorEntity cnvv set cnvv.idCompensacionLabNivelVigencia =:idCompensacionLabNivelVigencia, cnvv.idRegla=:idRegla, " +
                    "cnvv.idVariable =:idVariable, cnvv.fechaCambio =:fechaCambio, cnvv.registradoPor =:registradoPor where cnvv.id =:id")
    int update (@Param("idCompensacionLabNivelVigencia") Long idCompensacionLabNivelVigencia,
                @Param("idRegla") Long idRegla,
                @Param("idVariable") Long idVariable,
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_COMPENSACIONLABNIVELVIGVALOR(?1, ?2)",nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);
}