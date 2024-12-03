package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.AccionEntity;

public interface AccionDAO extends JpaRepository<AccionEntity, Long>, JpaSpecificationExecutor<AccionEntity> {

    @Modifying
    @Query(value = """
        update AccionEntity a set a.nombre =:nombre, a.claseIcono =:claseIcono,  a.claseEstado = :claseEstado,
        a.path =:path,  a.fechaCambio = :fechaCambio, a.registradoPor = :registradoPor where a.id = :id
    """)
    int update(@Param("nombre") String nombre,
               @Param("claseIcono") String claseIcono,
               @Param("claseEstado") String claseEstado,
               @Param("path") String path,
               @Param("fechaCambio") Date fechaCambio,
               @Param("registradoPor") String registradoPor,
               @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_ACCION(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);

    @Query(value = "SELECT FORTALECIMIENTO.pr_fortalecimiento_i_accion(?1,?2,?3,?4,?5)", nativeQuery = true)
    Integer saveActionProcedure(String nombre,
                                String claseIcono,
                                String claseEstado,
                                String path,
                                String registradoPor
    );
}
