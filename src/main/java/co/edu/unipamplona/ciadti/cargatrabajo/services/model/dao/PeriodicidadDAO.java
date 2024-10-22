package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.PeriodicidadEntity;

public interface PeriodicidadDAO extends JpaRepository<PeriodicidadEntity, Long>, JpaSpecificationExecutor<PeriodicidadEntity>{

    @Modifying
    @Query(value = "update PeriodicidadEntity p set p.nombre =:nombre, p.frecuenciaAnual =:frecuenciaAnual, " +
                    "p.fechaCambio =:fechaCambio, p.registradoPor =:registradoPor where p.id =:id")
    int update (@Param("nombre") String nombre,
                @Param("frecuenciaAnual") Long frecuenciaAnual,
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);
    
    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENT0_D_PERIODICIDAD(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);
}
