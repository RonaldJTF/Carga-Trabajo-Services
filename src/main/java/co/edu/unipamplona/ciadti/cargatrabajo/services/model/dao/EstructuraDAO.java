package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.EstructuraEntity;

public interface EstructuraDAO extends JpaRepository<EstructuraEntity, Long>, JpaSpecificationExecutor<EstructuraEntity>{

    @Modifying
    @Query(value =  "update EstructuraEntity e set e.nombre = :nombre, e.descripcion = :descripcion, e.idPadre = :idPadre, " +
                    "e.idTipologia = :idTipologia, e.icono = :icono, e.mimetype = :mimetype,  e.fechaCambio = :fechaCambio, " + 
                    "e.registradoPor = :registradoPor where e.id = :id")
    int update( @Param("nombre") String nombre,
                @Param("descripcion") String descripcion,
                @Param("idPadre") Long idPadre,
                @Param("idTipologia") Long idTipologia,
                @Param("icono") byte[] icono,
                @Param("mimetype") String mimetype,
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_ESTRUCTURA(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);
}   
