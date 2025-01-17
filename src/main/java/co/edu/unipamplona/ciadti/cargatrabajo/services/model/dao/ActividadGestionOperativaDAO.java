package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ActividadGestionOperativaEntity;

public interface ActividadGestionOperativaDAO extends JpaRepository<ActividadGestionOperativaEntity, Long>, JpaSpecificationExecutor<ActividadGestionOperativaEntity>{
    
    @Modifying
    @Query(value = """
        update ActividadGestionOperativaEntity ago set ago.idActividad = :idActividad, ago.idGestionOperativa = :idGestionOperativa,
        ago.fechaCambio = :fechaCambio, ago.registradoPor = :registradoPor where ago.id = :id
    """)
    int update(@Param("idActividad") Long idActividad,
               @Param("idGestionOperativa") Long idGestionOperativa,
               @Param("fechaCambio") Date fechaCambio,
               @Param("registradoPor") String registradoPor,
               @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_ACTIVIDADGESTIONOPERATIVA(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure (Long id, String registradoPor);

    @Query(value = "SELECT ago FROM ActividadGestionOperativaEntity ago WHERE ago.idGestionOperativa = :idGestionOperativa")
    ActividadGestionOperativaEntity findByIdGestionOperativa(@Param("idGestionOperativa") Long idGestionOperativa);
}
