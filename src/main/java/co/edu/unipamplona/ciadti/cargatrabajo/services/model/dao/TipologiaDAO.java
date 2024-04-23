package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.TipologiaEntity;

public interface TipologiaDAO extends JpaRepository<TipologiaEntity, Long>, JpaSpecificationExecutor<TipologiaEntity>{

    @Modifying
    @Query(value = "update TipologiaEntity t set t.idTipologiaSiguiente =:idTipologiaSiguiente, t.nombre =:nombre,  t.claseIcono = :claseIcono, " + 
                    "t.nombreColor =:nombreColor, t.esDependencia =:esDependencia,  t.fechaCambio = :fechaCambio, " + 
                    "t.registradoPor = :registradoPor where  t.id = :id")
    int update (@Param("idTipologiaSiguiente") Long idTipologiaSiguiente,
                @Param("nombre") String nombre,
                @Param("claseIcono") String claseIcono,
                @Param("nombreColor") String nombreColor,
                @Param("esDependencia") String esDependencia,
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_TIPOLOGIA(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);
}
