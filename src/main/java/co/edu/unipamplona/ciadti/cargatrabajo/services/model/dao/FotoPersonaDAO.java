package co.edu.unipamplona.ciadti.cargatrabajo.services.model.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unipamplona.ciadti.cargatrabajo.services.model.entity.FotoPersonaEntity;

public interface FotoPersonaDAO extends JpaRepository<FotoPersonaEntity, Long>, JpaSpecificationExecutor<FotoPersonaEntity>{

    @Modifying
    @Query(value = """
        update FotoPersonaEntity fp set fp.idPersona = :idPersona, fp.archivo =:archivo, fp.mimetype = :mimetype, 
        fp.fechaCambio = :fechaCambio, fp.registradoPor = :registradoPor where fp.id=:id
    """)
    int update (@Param("idPersona") Long idPersona,
                @Param("archivo") byte[] archivo,
                @Param("mimetype") String mimetype, 
                @Param("fechaCambio") Date fechaCambio,
                @Param("registradoPor") String registradoPor,
                @Param("id") Long id);

    @Query(value = "SELECT FORTALECIMIENTO.PR_FORTALECIMIENTO_D_FOTOPERSONA(?1, ?2)", nativeQuery = true)
    Integer deleteByProcedure(Long id, String registradoPor);

    FotoPersonaEntity findByIdPersona(Long idPersona);
}
