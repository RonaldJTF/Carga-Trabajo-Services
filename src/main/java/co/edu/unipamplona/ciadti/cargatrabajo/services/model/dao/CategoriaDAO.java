package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.CategoriaEntity;

public interface CategoriaDAO extends JpaRepository<CategoriaEntity, Long>, JpaSpecificationExecutor<CategoriaEntity>{
    
    @Modifying
    @Query(value = "update CategoriaEntity c set c.nombre =:nombre, c.descripcion =:descripcion, " +
                    "c.fechaCambio =:fechaCambio, c.registradoPor =:registradoPor where c.id =:id")
    int update (@Param("nombre") String nombre,
                @Param("descripcion") String descripcion,
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_CATEGORIA(?1, ?2)",nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);
    
}
