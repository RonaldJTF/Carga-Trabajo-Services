package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.GeneroEntity;

public interface GeneroDAO extends JpaRepository<GeneroEntity, Long>, JpaSpecificationExecutor<GeneroEntity>{

    @Modifying
    @Query(value = """
        update GeneroEntity g set g.nombre =:nombre, g.fechaCambio = :fechaCambio, g.registradoPor = :registradoPor 
        where g.id=:id
    """)
    int update (@Param("nombre") String nombre,
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_GENERO(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure (Long id, String registradoPor);
}
