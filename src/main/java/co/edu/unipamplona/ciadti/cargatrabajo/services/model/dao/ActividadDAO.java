package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ActividadEntity;

public interface ActividadDAO extends JpaRepository<ActividadEntity, Long>, JpaSpecificationExecutor<ActividadEntity>{

    @Modifying
    @Query(value = "update ActividadEntity a set a.idNivel = :idNivel, a.idEstructura = :idEstructura, a.frecuencia =:frecuencia, " + 
                    "a.tiempoMaximo = :tiempoMaximo, a.tiempoMinimo =:tiempoMinimo, a.tiempoPromedio = :tiempoPromedio, " + 
                    " a.fechaCambio = :fechaCambio, a.registradoPor =:registradoPor where a.id = :id")
    int update(@Param("idNivel") Long idNivel,
               @Param("idEstructura") Long idEstructura,
               @Param("frecuencia") Double frecuencia,
               @Param("tiempoMaximo") Double tiempoMaximo,
               @Param("tiempoMinimo") Double tiempoMinimo,
               @Param("tiempoPromedio") Double tiempoPromedio,               
               @Param("fechaCambio") Date fechaCambio,
               @Param("registradoPor") String registradoPor,
               @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_ACTIVIDAD(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);
}
