package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.GestionOperativaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.OrganigramaEntity;

public interface OrganigramaDAO extends JpaRepository<OrganigramaEntity, Long>, JpaSpecificationExecutor<OrganigramaEntity>{
    
    @Modifying
    @Query(value = """
        update OrganigramaEntity o set o.nombre = :nombre,  o.descripcion = :descripcion, o.idNormatividad = :idNormatividad,
        o.diagrama = :diagrama, o.mimetype = :mimetype,  o.fechaCambio = :fechaCambio, o.registradoPor = :registradoPor where o.id = :id
    """)
    int update(@Param("nombre") String nombre,
               @Param("descripcion") String descripcion,
               @Param("idNormatividad") Long idNormatividad,
               @Param("diagrama") byte[] diagrama,
               @Param("mimetype") String mimetype,
               @Param("fechaCambio") Date fechaCambio,
               @Param("registradoPor") String registradoPor,
               @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_ORGANIGRAMA(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);
}
