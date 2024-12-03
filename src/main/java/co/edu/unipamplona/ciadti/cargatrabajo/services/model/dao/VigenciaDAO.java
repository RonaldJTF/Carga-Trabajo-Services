package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.VigenciaEntity;

public interface VigenciaDAO extends JpaRepository<VigenciaEntity, Long>, JpaSpecificationExecutor<VigenciaEntity>{
    
    @Modifying
    @Query(value = """
        update VigenciaEntity v set v.nombre =:nombre, v.anio =:anio, 
        v.estado =:estado, v.fechaCambio =:fechaCambio, v.registradoPor =:registradoPor where v.id =:id
    """)
    int update (@Param("nombre") String nombre,
                @Param("anio") String anio,
                @Param("estado") String estado,
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);
    
    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_VIGENCIA(?1 , ?2 )", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);

    @Modifying
    @Query(value = "update VigenciaEntity v set v.estado =:state")
    int updateStateToAllValidities(@Param("state") String state);

}
