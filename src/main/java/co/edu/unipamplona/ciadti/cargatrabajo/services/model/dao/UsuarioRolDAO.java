package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.UsuarioRolEntity;

public interface UsuarioRolDAO extends JpaRepository<UsuarioRolEntity, Long>, JpaSpecificationExecutor<UsuarioRolEntity>{

    @Modifying
    @Query(value = "update UsuarioRolEntity ur set ur.idUsuario =:idUsuario, ur.idRol =:idRol, " + 
                    "ur.fechaCambio = :fechaCambio, ur.registradoPor = :registradoPor where  ur.id = :id")
    void update(@Param("idUsuario") Long idUsuario,
                @Param("idRol") Long idRol,
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);


    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_USUARIOROL(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);


    List<UsuarioRolEntity> findAllByIdUsuario(Long idUsuario);

    UsuarioRolEntity findByIdUsuarioAndIdRol(Long idUsuario, Long idRol);
}
