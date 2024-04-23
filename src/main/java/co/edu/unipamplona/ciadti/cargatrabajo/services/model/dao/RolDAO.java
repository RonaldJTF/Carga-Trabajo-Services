package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.RolEntity;

public interface RolDAO extends JpaRepository<RolEntity, Long>, JpaSpecificationExecutor<RolEntity>{

    @Modifying
    @Query(value = "update RolEntity r set r.nombre =:nombre, r.codigo =:codigo, " +
                   "r.fechaCambio = :fechaCambio, r.registradoPor = :registradoPor where r.id=:id")
    int update (@Param("nombre") String nombre,
                @Param("codigo") String codigo,
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_ROL(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);
}
