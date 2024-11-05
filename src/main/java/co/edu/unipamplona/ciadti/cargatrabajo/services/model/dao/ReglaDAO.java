package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ReglaEntity;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.VariableEntity;

public interface ReglaDAO extends JpaRepository<ReglaEntity, Long>, JpaSpecificationExecutor<ReglaEntity>{
    
    @Modifying
    @Query(value = "update ReglaEntity r set r.nombre =:nombre, r.descripcion =:descripcion, r.condiciones =:condiciones, " +
                    "r.global =:global, r.estado =:estado, r.fechaCambio =:fechaCambio, r.registradoPor =:registradoPor where r.id =:id")
    int update (@Param("nombre") String nombre,
                @Param("descripcion") String descripcion,
                @Param("condiciones") String condiciones,
                @Param("global") String global,
                @Param("estado") String estado,
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_REGLA(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);

    // @Modifying
    // @Transactional
    // @Query("UPDATE ReglaEntity r SET r.condiciones = :condiciones WHERE r.id = :id")
    // void updateCondiciones(@Param("id") Long id, @Param("condiciones") String condiciones);



    
}
