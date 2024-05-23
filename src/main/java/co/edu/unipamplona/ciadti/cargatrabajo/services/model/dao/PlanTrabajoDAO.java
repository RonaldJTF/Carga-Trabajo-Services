package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.PlanTrabajoEntity;

public interface PlanTrabajoDAO extends JpaRepository<PlanTrabajoEntity, Long>, JpaSpecificationExecutor<PlanTrabajoEntity>{
    
    @Modifying
    @Query(value = "update PlanTrabajoEntity pt set pt.nombre =:nombre, pt.descripcion =:descripcion, " +
                   "pt.fechaCambio = :fechaCambio, pt.registradoPor = :registradoPor where pt.id=:id")
    int update (@Param("nombre") String nombre,
                @Param("descripcion") String descripcion,
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_PLANTRABAJO(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);
}
