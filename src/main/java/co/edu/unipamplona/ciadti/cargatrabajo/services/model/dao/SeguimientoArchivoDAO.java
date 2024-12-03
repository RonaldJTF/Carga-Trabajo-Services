package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.SeguimientoArchivoEntity;

public interface SeguimientoArchivoDAO extends JpaRepository<SeguimientoArchivoEntity, Long>, JpaSpecificationExecutor<SeguimientoArchivoEntity>{
    
    @Modifying
    @Query(value = """
        update SeguimientoArchivoEntity sa set sa.idSeguimiento = :idSeguimiento, sa.idArchivo =:idArchivo, 
        sa.fechaCambio = :fechaCambio, sa.registradoPor = :registradoPor where sa.id=:id
    """)
    int update (@Param("idSeguimiento") Long idSeguimiento,
                @Param("idArchivo") Long idArchivo,
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_SEGUIMIENTOARCHIVO(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);

    SeguimientoArchivoEntity findByIdSeguimientoAndIdArchivo(Long idSeguimiento, Long idArchivo);
}
