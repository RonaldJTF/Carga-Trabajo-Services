package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.JerarquiaGestionOperativaEntity;

public interface JerarquiaGestionOperativaDAO extends JpaRepository<JerarquiaGestionOperativaEntity, Long>, JpaSpecificationExecutor<JerarquiaGestionOperativaEntity> {
    
    @Modifying
    @Query(value = """
        update  JerarquiaGestionOperativaEntity jgo set jgo.idJerarquia = :idJerarquia, j.idGestionOperativa = :idGestionOperativa,
        jgo.fechaCambio = :fechaCambio, jgo.registradoPor = :registradoPor where jgo.id = :id
    """)
    int update(@Param("idJerarquia") Long idJerarquia,
               @Param("idGestionOperativa") Long idGestionOperativa,
               @Param("fechaCambio") Date fechaCambio,
               @Param("registradoPor") String registradoPor,
               @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_JERARQUIAGESTIONOPERATIVA(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure (Long id, String registradoPor);
}
