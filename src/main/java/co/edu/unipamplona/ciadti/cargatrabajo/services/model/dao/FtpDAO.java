package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.FtpEntity;

public interface FtpDAO extends JpaRepository<FtpEntity, Long>, JpaSpecificationExecutor<FtpEntity>{
    
    @Modifying
    @Query(value = """
        update FtpEntity f set f.nombre =:nombre, f.descripcion =:descripcion, f.codigo = :codigo,
        f.activo =:activo, f.fechaCambio = :fechaCambio, f.registradoPor = :registradoPor where f.id=:id
    """)
    int update (@Param("nombre") String nombre,
                @Param("descripcion") String descripcion,
                @Param("codigo") String codigo,
                @Param("activo") String activo,
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_FTP(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);

    @Query(value = "select f from FtpEntity as f where f.activo = '1'")
    Optional<FtpEntity> findActive();
}
