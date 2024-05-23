package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EtapaEntity;

public interface EtapaDAO extends JpaRepository<EtapaEntity, Long>, JpaSpecificationExecutor<EtapaEntity> {
    
    @Modifying
    @Query(value = "update EtapaEntity e set e.nombre =:nombre, e.descripcion =:descripcion, e.idPadre = :idPadre, " +
                   "e.idPlanTrabajo =:idPlanTrabajo, e.fechaCambio = :fechaCambio, e.registradoPor = :registradoPor where e.id=:id")
    int update (@Param("nombre") String nombre,
                @Param("descripcion") String descripcion,
                @Param("idPadre") Long idPadre,
                @Param("idPlanTrabajo") Long idPlanTrabajo,
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_ETAPA(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);

    List<EtapaEntity> findAllByIdPlanTrabajo(Long idPlanTrabajo);

    List<EtapaEntity> findAllByIdPadre(Long id);
}
