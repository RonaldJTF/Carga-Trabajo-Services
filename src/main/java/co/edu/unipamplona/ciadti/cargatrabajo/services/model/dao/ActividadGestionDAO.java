package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ActividadGestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;

public interface ActividadGestionDAO extends JpaRepository<ActividadGestionEntity, Long>, JpaSpecificationExecutor<ActividadGestionEntity> {
    @Modifying
    @Query(value = """
        update ActividadGestionEntity a set a.idNivel = :idNivel, a.idGestionOperativa = :idGestionOperativa, a.frecuencia =:frecuencia,
        a.tiempoMaximo = :tiempoMaximo, a.tiempoMinimo =:tiempoMinimo, a.tiempoPromedio = :tiempoPromedio, 
        a.fechaCambio = :fechaCambio, a.registradoPor =:registradoPor where a.id = :id
    """)
    int update(@Param("idNivel") Long idNivel,
               @Param("idGestionOperativa") Long idGestionOperativa,
               @Param("frecuencia") Double frecuencia,
               @Param("tiempoMaximo") Double tiempoMaximo,
               @Param("tiempoMinimo") Double tiempoMinimo,
               @Param("tiempoPromedio") Double tiempoPromedio,
               @Param("fechaCambio") Date fechaCambio,
               @Param("registradoPor") String registradoPor,
               @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_ACTIVIDADGESTION(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_ACTIVIDADGESTIONOPERATIVA(?1, ?2)", nativeQuery = true)
    Integer deleteActividadGestioOperativaByProcedure(Long id, String register);

    ActividadGestionEntity findByIdGestionOperativa(Long idGestionOperativa);
}
