package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.ArchivoEntity;

public interface ArchivoDAO extends JpaRepository<ArchivoEntity, Long>, JpaSpecificationExecutor<ArchivoEntity>{
    
    @Modifying
    @Query(value = """
        update ArchivoEntity a set a.idFtp = :idFtp, a.nombre =:nombre, a.path =:path, a.tamanio = :tamanio, 
        a.mimetype =:mimetype, a.fechaCambio = :fechaCambio, a.registradoPor = :registradoPor where a.id=:id
    """)
    int update (@Param("idFtp") Long idFtp,
                @Param("nombre") String nombre,
                @Param("path") String path,
                @Param("tamanio") Long tamanio,
                @Param("mimetype") String mimetype,
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_ARCHIVO(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);

    @Query(value = "select a from ArchivoEntity as a inner join SeguimientoArchivoEntity sa on (a.id = sa.idArchivo) where sa.idSeguimiento = :idSeguimiento ")
    List<ArchivoEntity> findAllByIdSeguimiento(@Param("idSeguimiento") Long idSeguimiento);
}
