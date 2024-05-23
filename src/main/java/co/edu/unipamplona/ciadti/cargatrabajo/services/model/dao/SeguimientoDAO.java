package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.SeguimientoEntity;

public interface SeguimientoDAO extends JpaRepository<SeguimientoEntity, Long>, JpaSpecificationExecutor<SeguimientoEntity>{
    
    @Modifying
    @Query(value = "update SeguimientoEntity s set s.idTarea = :idTarea, s.porcentajeAvance =:porcentajeAvance, s.observacion =:observacion, " +
                   "s.activo =:activo, s.fechaCambio = :fechaCambio, s.registradoPor = :registradoPor where s.id=:id")
    int update (@Param("idTarea") Long idTarea,
                @Param("porcentajeAvance") Double porcentajeAvance,
                @Param("observacion") String observacion,
                @Param("activo") String activo,
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_SEGUIMIENTO(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);

    List<SeguimientoEntity> findAllByIdTarea(Long idTarea);
}
