package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.JerarquiaEntity;

public interface JerarquiaDAO extends JpaRepository<JerarquiaEntity, Long>, JpaSpecificationExecutor<JerarquiaEntity>{

    @Modifying
    @Query(value = """
        update  JerarquiaEntity j set j.idOrganigrama = :idOrganigrama, j.idDependencia = :idDependencia,
        j.idDependenciaPadre = :idDependenciaPadre, j.orden = :orden,
        j.fechaCambio = :fechaCambio, j.registradoPor = :registradoPor where j.id = :id
    """)
    int update(@Param("idOrganigrama") Long idOrganigrama,
               @Param("idDependencia") Long idDependencia,
               @Param("idDependenciaPadre") Long idDependenciaPadre,
               @Param("orden") Long orden,
               @Param("fechaCambio") Date fechaCambio,
               @Param("registradoPor") String registradoPor,
               @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_JERARQUIA(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure (Long id, String registradoPor);

    @Query(value = """
        SELECT j FROM JerarquiaEntity j WHERE j.idOrganigrama = :idOrganigrama AND j.idDependencia = :idDependencia       
    """)
    JerarquiaEntity findByIdOrganigramaAndIdDependencia(@Param("idOrganigrama") Long idOrganigrama, @Param("idDependencia") Long idDependencia);
}
