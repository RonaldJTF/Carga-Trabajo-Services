package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ValorVigenciaEntity;

public interface ValorVigenciaDAO extends JpaRepository<ValorVigenciaEntity, Long>, JpaSpecificationExecutor<ValorVigenciaEntity>{
    
    @Modifying
    @Query(value = "update ValorVigenciaEntity vv set vv.idVariable =:idVariable, vv.idVigencia =:idVigencia, " +
                    "vv.valor =:valor, vv.fechaCambio =:fechaCambio, vv.registradoPor =:registradoPor where vv.id =:id")
    int update (@Param("idVariable") Long idVariable,
                @Param("idVigencia") Long idVigencia,
                @Param("valor") Long valor,
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_VALORVIGENCIA(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);

    @Query(value = "select vv.valor  from ValorVigenciaEntity vv " + 
                   "where vv.idVariable = :idVariable and vv.idVigencia  = :idVigencia")
    Double findValueInValidity(@Param("idVariable") Long variableId, @Param("idVigencia") Long validityId);
}
