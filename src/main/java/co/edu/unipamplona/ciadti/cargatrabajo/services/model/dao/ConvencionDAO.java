package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ConvencionEntity;

public interface ConvencionDAO extends JpaRepository<ConvencionEntity, Long>, JpaSpecificationExecutor<ConvencionEntity> {
    
    @Modifying
    @Query(value = """
        update ConvencionEntity c set c.nombre = :nombre, c.descripcion = :descripcion, c.claseIcono = :claseIcono,
        c.nombreColor = :nombreColor, c.fechaCambio = :fechaCambio, c.registradoPor = :registradoPor where c.id = :id
    """)
    int update(@Param("nombre") String nombre,
               @Param("descripcion") String descripcion,
               @Param("claseIcono") String claseIcono,
               @Param("nombreColor") String nombreColor,
               @Param("fechaCambio") Date fechaCambio,
               @Param("registradoPor") String registradoPor,
               @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_CONVENCION(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure (Long id, String registradoPor);
}
