package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.DenominacionEmpleoEntity;

public interface DenominacionEmpleoDAO extends JpaRepository<DenominacionEmpleoEntity, Long>, JpaSpecificationExecutor<DenominacionEmpleoEntity>{
    
    @Modifying
    @Query(value = """
        update DenominacionEmpleoEntity de set de.nombre = :nombre,  de.descripcion = :descripcion,
        de.fechaCambio = :fechaCambio, de.registradoPor = :registradoPor where de.id = :id
    """)
    int update(@Param("nombre") String nombre,
               @Param("descripcion") String descripcion,
               @Param("fechaCambio") Date fechaCambio,
               @Param("registradoPor") String registradoPor,
               @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_DENOMINACIONEMPLEO(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);
}
