package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.DependenciaEntity;

public interface DependenciaDAO extends JpaRepository<DependenciaEntity, Long>, JpaSpecificationExecutor<DependenciaEntity>{
    
    @Modifying
    @Query(value = """
        update DependenciaEntity d set d.nombre = :nombre,  d.descripcion = :descripcion, d.idConvencion = :idConvencion,
        d.icono = :icono, d.mimetype = :mimetype, d.fechaCambio = :fechaCambio, d.registradoPor = :registradoPor
        where d.id = :id
    """)
    int update(@Param("nombre") String nombre,
               @Param("descripcion") String descripcion,
               @Param("idConvencion") Long idConvencion,
               @Param("icono") byte[] icono,
               @Param("mimetype") String mimetype,
               @Param("fechaCambio") Date fechaCambio,
               @Param("registradoPor") String registradoPor,
               @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_DEPENDENCIA(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);

    @Query(value = """
        SELECT d FROM DependenciaEntity d 
        INNER JOIN JerarquiaEntity j ON (d.id = j.idDependencia)
        WHERE j.id = :hierarchyId        
    """)
    DependenciaEntity findByHierarchyId(@Param ("hierarchyId") Long hierarchyId);
}
