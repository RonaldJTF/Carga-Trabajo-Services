package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.AlcanceEntity;

public interface AlcanceDAO extends JpaRepository<AlcanceEntity, Long>, JpaSpecificationExecutor<AlcanceEntity>{
    
    @Modifying
    @Query(value = "update AlcanceEntity a set a.nombre =:nombre, a.descripcion =:descripcion, " + 
                    "a.fechaCambio =:fechaCambio,  a.registradoPor =:registradoPor where a.id =:id")
    int update  (@Param("nombre") String nombre,
                 @Param("descripcion") String descripcion,
                 @Param("fechaCambio") Date fechaCambio,
                 @Param("RegistradoPor") String registradoPor,
                 @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_ALCANCE(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);
}
